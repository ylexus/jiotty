package net.yudichev.jiotty.connector.shelly;

import com.google.common.collect.ImmutableList;
import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.common.async.ExecutorFactory;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.async.backoff.RetryableOperationExecutor;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.Closeable;
import net.yudichev.jiotty.common.time.CurrentDateTimeProvider;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.yudichev.jiotty.common.lang.Closeable.closeSafelyIfNotNull;
import static net.yudichev.jiotty.common.lang.HumanReadableExceptionMessage.humanReadableMessage;
import static net.yudichev.jiotty.common.lang.Locks.inLock;
import static net.yudichev.jiotty.common.rest.RestClients.call;
import static net.yudichev.jiotty.common.rest.RestClients.newClient;
import static net.yudichev.jiotty.connector.shelly.ShellyPlugImpl.SampleAggregator.MAX_SAMPLE_COUNT;

final class ShellyPlugImpl extends BaseLifecycleComponent implements ShellyPlug {
    private static final Logger logger = LoggerFactory.getLogger(ShellyPlugImpl.class);

    private final OkHttpClient httpClient = newClient();
    private final String host;
    private final ExecutorFactory executorFactory;
    private final RetryableOperationExecutor retryableOperationExecutor;
    private final String baseUrl;
    private final CurrentDateTimeProvider timeProvider;

    private SchedulingExecutor executor;

    @Nullable
    private ConsumptionMeasurementImpl activeMeasurement;

    @Inject
    ShellyPlugImpl(@Host String host,
                   ExecutorFactory executorFactory,
                   @Dependency RetryableOperationExecutor retryableOperationExecutor,
                   CurrentDateTimeProvider timeProvider) {
        this.host = checkNotNull(host);
        this.executorFactory = checkNotNull(executorFactory);
        this.retryableOperationExecutor = checkNotNull(retryableOperationExecutor);
        this.timeProvider = checkNotNull(timeProvider);
        baseUrl = "http://" + host;
    }

    @Override
    protected void doStart() {
        executor = executorFactory.createSingleThreadedSchedulingExecutor("ShellyPlug-" + host);
    }

    @Override
    protected void doStop() {
        closeSafelyIfNotNull(logger, executor);
    }

    @Override
    public ConsumptionMeasurement startMeasuringConsumption(Consumer<String> errorHandler) {
        return whenStartedAndNotLifecycling(() -> {
            checkState(activeMeasurement == null, "Already started measuring consumption");
            return activeMeasurement = new ConsumptionMeasurementImpl(errorHandler);
        });
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Host {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }

    private class ConsumptionMeasurementImpl implements ConsumptionMeasurement {

        private static final Duration SAMPLING_PERIOD = Duration.ofMinutes(1);

        private final Request request = new Request.Builder().url(baseUrl + "/rpc/Switch.GetStatus?id=0").get().build();
        private final Consumer<String> errorHandler;

        private final Lock lock = new ReentrantLock();
        /**
         * {@code null} means we are stopped
         */
        @Nullable
        private SampleAggregator sampleAggregator = new SampleAggregator();
        private Instant sampleStartTime;
        @Nullable
        private Closeable nextSamplingSchedule;

        public ConsumptionMeasurementImpl(Consumer<String> errorHandler) {
            this.errorHandler = checkNotNull(errorHandler);
            logger.info("Starting sampling consumption");
            executor.execute(this::sample);
        }

        @Override
        public Optional<ConsumptionCurve> stop() {
            logger.info("Stopping consumption sampling");
            var curve = inLock(lock, () -> {
                checkState(isRunning(), "Already stopped");
                Optional<ConsumptionCurve> result = sampleAggregator.generateConsumptionCurve();
                stopAndReleaseResources();
                return result;
            });
            // allow starting new measurements
            whenStartedAndNotLifecycling(() -> {
                activeMeasurement = null;
            });
            return curve;
        }

        private void sample() {
            sampleStartTime = timeProvider.currentInstant();
            retryableOperationExecutor.withBackOffAndRetry("Shelly-Switch.GetStatus-" + host,
                                                           () -> call(httpClient.newCall(request), SwitchStatus.class))
                                      .whenCompleteAsync((switchStatus, e) -> {
                                          if (inLock(lock, this::isRunning)) {
                                              processResponse(switchStatus, e);
                                          } else {
                                              logger.debug("Discarded response after stop: {}", switchStatus, e);
                                          }
                                      }, executor);
        }

        @SuppressWarnings("TypeMayBeWeakened")
        private void processResponse(SwitchStatus switchStatus, Throwable e) {
            logger.debug("Processing response {}", switchStatus);
            if (e != null) {
                inLock(lock, this::stopAndReleaseResources);
                sendError(humanReadableMessage(e));
            } else {
                try {
                    boolean canProceed = processResponse(switchStatus.energyStatus());
                    if (canProceed) {
                        var nextSampleStartTime = sampleStartTime.plus(SAMPLING_PERIOD);
                        var delayUntilNextSample = Duration.between(timeProvider.currentInstant(), nextSampleStartTime);
                        if (delayUntilNextSample.isNegative()) {
                            delayUntilNextSample = Duration.ZERO;
                        }
                        logger.debug("Processed, next sample in {}", delayUntilNextSample);
                        nextSamplingSchedule = executor.schedule(delayUntilNextSample, this::sample);
                    }
                } catch (RuntimeException ex) {
                    logger.error("Consumption response processing failed", ex);
                }
            }
        }

        /**
         * @return {@code true} if can processed, otherwise indicates we are stopped
         */
        private boolean processResponse(SwitchEnergyStatus switchEnergyStatus) {
            enum Outcome {EXCEEDED_MAX_SIZE, OK, ALREADY_STOPPED}
            Outcome outcome = inLock(lock, () -> {
                if (isRunning()) {
                    var weExceededMaxSize = sampleAggregator.processResponse(switchEnergyStatus);
                    if (weExceededMaxSize) {
                        stopAndReleaseResources();
                        return Outcome.EXCEEDED_MAX_SIZE;
                    }
                    return Outcome.OK;
                } else {
                    logger.info("Ignored response after stop: {}", switchEnergyStatus);
                    return Outcome.ALREADY_STOPPED;
                }
            });
            return switch (outcome) {
                case OK -> true;
                case EXCEEDED_MAX_SIZE -> {
                    sendError("Sample count exceeded max size " + MAX_SAMPLE_COUNT);
                    yield false;
                }
                case ALREADY_STOPPED -> false;
            };
        }

        private void sendError(String errorMessage) {
            try {
                errorHandler.accept(errorMessage);
            } catch (RuntimeException ex) {
                logger.error("Error handler failed", ex);
            }
        }

        private boolean isRunning() {
            return sampleAggregator != null;
        }

        private void stopAndReleaseResources() {
            closeSafelyIfNotNull(logger, nextSamplingSchedule);
            nextSamplingSchedule = null;
            sampleAggregator = null;
        }
    }

    static class SampleAggregator {
        public static final int MAX_SAMPLE_COUNT = 10_000;

        private final TreeMap<Long, Double> consumptionByEpochSec = new TreeMap<>();

        @SuppressWarnings("TypeMayBeWeakened")
        public boolean processResponse(SwitchEnergyStatus switchEnergyStatus) {
            var timeSec = switchEnergyStatus.endOfNewestMinuteEpochTimeSec();
            for (Double consumption : switchEnergyStatus.mWHoursByMinute()) {
                var previousValue = consumptionByEpochSec.putIfAbsent(timeSec, consumption);
                if (logger.isDebugEnabled() && previousValue == null) {
                    logger.debug("Added sample {}->{}", Instant.ofEpochSecond(timeSec), consumption);
                }
                timeSec -= 60;
            }
            return consumptionByEpochSec.size() > MAX_SAMPLE_COUNT;
        }

        public Optional<ConsumptionCurve> generateConsumptionCurve() {
            if (consumptionByEpochSec.isEmpty()) {
                return Optional.empty();
            }
            var sampleListBuilder = ImmutableList.<Double>builderWithExpectedSize(consumptionByEpochSec.size() * 12 / 10);
            var curTime = consumptionByEpochSec.firstKey();
            var endTime = consumptionByEpochSec.lastKey();
            while (curTime <= endTime) {
                // find the entry nearest to the current time
                Map.Entry<Long, Double> floorEntry = consumptionByEpochSec.floorEntry(curTime);
                Map.Entry<Long, Double> ceilingEntry = consumptionByEpochSec.ceilingEntry(curTime);
                var distanceToFloor = curTime - floorEntry.getKey();
                var distanceToCeiling = ceilingEntry.getKey() - curTime;
                assert distanceToFloor >= 0 && distanceToCeiling >= 0;
                sampleListBuilder.add(distanceToFloor < distanceToCeiling ? floorEntry.getValue() : ceilingEntry.getValue());
                // next minute
                curTime += 60;
            }
            return Optional.of(new ConsumptionCurve(Instant.ofEpochSecond(consumptionByEpochSec.firstKey()), sampleListBuilder.build()));
        }
    }
}
