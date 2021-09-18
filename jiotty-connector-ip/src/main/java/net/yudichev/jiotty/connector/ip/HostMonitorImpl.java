package net.yudichev.jiotty.connector.ip;

import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.common.async.DispatchedConsumer;
import net.yudichev.jiotty.common.async.ExecutorFactory;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.Closeable;
import net.yudichev.jiotty.common.lang.DeduplicatingConsumer;
import net.yudichev.jiotty.common.lang.StabilisingConsumer;
import net.yudichev.jiotty.common.time.CurrentDateTimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.yudichev.jiotty.common.lang.Closeable.closeSafelyIfNotNull;
import static net.yudichev.jiotty.common.lang.Closeable.idempotent;
import static net.yudichev.jiotty.common.lang.EqualityComparator.referenceEquality;
import static net.yudichev.jiotty.common.lang.HumanReadableExceptionMessage.humanReadableMessage;
import static net.yudichev.jiotty.connector.ip.HostMonitor.Status.DOWN;
import static net.yudichev.jiotty.connector.ip.HostMonitor.Status.UP;

final class HostMonitorImpl extends BaseLifecycleComponent implements HostMonitor {
    private static final Logger logger = LoggerFactory.getLogger(HostMonitorImpl.class);

    private final ExecutorFactory executorFactory;
    private final CurrentDateTimeProvider currentDateTimeProvider;
    private final String hostname;
    private final String name;
    private final InetAddressResolver inetAddressResolver;
    private final Duration tolerance;
    private final Duration periodBetweenPings;

    private final CopyOnWriteArrayList<Consumer<Status>> listeners = new CopyOnWriteArrayList<>();
    private Consumer<Status> statusStabiliser;
    private volatile SchedulingExecutor executor;
    @Nullable
    private Instant lastSuccessfulPing;
    @Nullable
    private Status currentStatus;
    @Nullable
    private Status currentStableStatus;
    private Closeable pingSchedule = Closeable.noop();

    @Inject
    HostMonitorImpl(ExecutorFactory executorFactory,
                    CurrentDateTimeProvider currentDateTimeProvider,
                    @Hostname String hostname,
                    @Name String name,
                    @Tolerance Duration tolerance) {
        this(executorFactory, currentDateTimeProvider, hostname, name, tolerance, InetAddress::getByName);
    }

    HostMonitorImpl(ExecutorFactory executorFactory,
                    CurrentDateTimeProvider currentDateTimeProvider,
                    String hostname,
                    String name,
                    Duration tolerance,
                    InetAddressResolver inetAddressFactory) {
        this.executorFactory = checkNotNull(executorFactory);
        this.currentDateTimeProvider = checkNotNull(currentDateTimeProvider);
        this.hostname = checkNotNull(hostname);
        this.name = checkNotNull(name);
        inetAddressResolver = checkNotNull(inetAddressFactory);
        checkState(tolerance.compareTo(Duration.ofSeconds(5)) >= 0,
                "tolerance must be >= 5 seconds, but was %s", tolerance);
        this.tolerance = checkNotNull(tolerance);
        periodBetweenPings = tolerance.dividedBy(10);
    }

    @Override
    public Closeable addListener(Consumer<Status> statusConsumer, Executor executor) {
        return whenStartedAndNotLifecycling(() -> {
            Consumer<Status> consumer = new DispatchedConsumer<>(new DeduplicatingConsumer<>(referenceEquality(), statusConsumer), executor);
            listeners.add(consumer);
            this.executor.execute(() -> {
                if (currentStableStatus != null) {
                    notifyListener(consumer);
                }
            });
            return idempotent(() -> listeners.remove(consumer));
        });
    }

    @Override
    protected void doStart() {
        logger.info("Start monitoring {} ({}) with tolerance {}", name, hostname, tolerance);
        executor = executorFactory.createSingleThreadedSchedulingExecutor("host-monitor-" + name);
        statusStabiliser = new DeduplicatingConsumer<>(referenceEquality(),
                new StabilisingConsumer<>(executor, tolerance, this::onStableStatus));

        executor.execute(() -> {
            onStatus(UP, "Assume UP on startup");
            ping();
        });
    }

    @Override
    protected void doStop() {
        SchedulingExecutor executor = this.executor;
        this.executor = null;
        executor.execute(() -> {
            pingSchedule.close();
            currentStatus = null;
            lastSuccessfulPing = null;
        });
        closeSafelyIfNotNull(logger, executor);
    }

    private void onStableStatus(Status status) {
        currentStableStatus = status;
        listeners.forEach(this::notifyListener);
    }

    private void scheduleNextPing() {
        whenStartedAndNotLifecycling(() -> {
            SchedulingExecutor executor = this.executor;
            if (executor != null) {
                pingSchedule = executor.schedule(periodBetweenPings, this::ping);
            }
        });
    }

    private void ping() {
        try {
            logger.debug("Ping {} ({})", name, hostname);
            InetAddress inetAddress = inetAddressResolver.resolve(hostname);
            logger.debug("{} resolved to {}", hostname, inetAddress);
            if (inetAddress.isReachable(5000)) {
                lastSuccessfulPing = currentDateTimeProvider.currentInstant();
                logger.debug("{} ({}) is reachable, lastSuccessfulPing={}", name, hostname, lastSuccessfulPing);
                onStatus(UP, "Reachable");
            } else {
                onUnreachable("Address unreachable");
            }
        } catch (IOException e) {
            onUnreachable(humanReadableMessage(e));
        }
        scheduleNextPing();
    }

    private void onUnreachable(String why) {
        logger.debug("{} ({}) is unreachable: {}, lastSuccessfulPing={}", name, hostname, why, lastSuccessfulPing);
        onStatus(DOWN, why);
    }

    private void onStatus(Status status, String description) {
        if (status != currentStatus) {
            logger.info("{} ({}) provisionally {}->{} ({})", name, hostname, currentStatus, status, description);
            currentStatus = status;
            statusStabiliser.accept(currentStatus);
        }
    }

    private void notifyListener(Consumer<Status> consumer) {
        logger.debug("Notify consumer {} about status {}", consumer, currentStableStatus);
        consumer.accept(currentStableStatus);
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Hostname {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Name {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Tolerance {
    }

    interface InetAddressResolver {
        InetAddress resolve(String hostname) throws UnknownHostException;
    }
}
