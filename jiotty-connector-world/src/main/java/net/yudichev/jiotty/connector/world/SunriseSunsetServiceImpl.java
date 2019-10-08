package net.yudichev.jiotty.connector.world;

import com.google.inject.assistedinject.Assisted;
import net.yudichev.jiotty.common.async.ExecutorFactory;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.Closeable;
import net.yudichev.jiotty.common.lang.CompositeRunnable;
import net.yudichev.jiotty.common.time.CurrentDateTimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.time.ZoneOffset.UTC;
import static java.util.concurrent.TimeUnit.SECONDS;
import static net.yudichev.jiotty.common.lang.Closeable.closeIfNotNull;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;

final class SunriseSunsetServiceImpl extends BaseLifecycleComponent implements SunriseSunsetService {
    private static final Logger logger = LoggerFactory.getLogger(SunriseSunsetServiceImpl.class);

    private final SunriseSunsetTimes sunriseSunsetTimes;
    private final CurrentDateTimeProvider currentDateTimeProvider;
    private final WorldCoordinates coordinates;
    private final SchedulingExecutor executor;

    private final CompositeRunnable sunsetHandlers = new CompositeRunnable();
    private final CompositeRunnable sunriseHandlers = new CompositeRunnable();

    private SunriseSunsetData currentSsData;

    private boolean sunIsUp;

    @Inject
    SunriseSunsetServiceImpl(ExecutorFactory executorFactory,
                             SunriseSunsetTimes sunriseSunsetTimes,
                             CurrentDateTimeProvider currentDateTimeProvider,
                             @Assisted WorldCoordinates coordinates) {
        this.sunriseSunsetTimes = checkNotNull(sunriseSunsetTimes);
        this.currentDateTimeProvider = checkNotNull(currentDateTimeProvider);
        this.coordinates = checkNotNull(coordinates);
        executor = executorFactory.createSingleThreadedSchedulingExecutor("sunrise-sunset-service");
    }

    @Override
    public Closeable onEverySunrise(Runnable action, Executor executor) {
        checkStarted();

        action = executedBy(action, executor);
        if (sunIsUp) {
            action.run();
        }
        return sunriseHandlers.add(action);
    }

    @Override
    public Closeable onEverySunset(Runnable action, Executor executor) {
        checkStarted();

        action = executedBy(action, executor);
        if (!sunIsUp) {
            action.run();
        }
        return sunsetHandlers.add(action);
    }

    @Override
    protected void doStart() {
        currentSsData = getAsUnchecked(() -> sunriseSunsetTimes.getCurrentSunriseSunset(coordinates).get(5, SECONDS));
        sunIsUp = calculateSunIsUp();
        executor.scheduleAtFixedRate(Duration.ZERO, Duration.ofDays(1), this::onTimesRefresh);
        executor.scheduleAtFixedRate(Duration.ofMinutes(7), this::onRefresh);
    }

    @Override
    protected void doStop() {
        closeIfNotNull(executor);
    }

    private static Runnable executedBy(Runnable action, Executor executor) {
        return () -> executor.execute(action);
    }

    private void onRefresh() {
        boolean newSunIsUp = calculateSunIsUp();
        try {
            if (!sunIsUp && newSunIsUp) {
                sunriseHandlers.run();
            } else if (sunIsUp && !newSunIsUp) {
                sunsetHandlers.run();
            }
        } finally {
            sunIsUp = newSunIsUp;
        }
    }

    private boolean calculateSunIsUp() {
        LocalTime localTime = currentDateTimeProvider.currentInstant().atZone(UTC).toLocalTime();
        LocalTime sunriseTime = currentSsData.sunrise().atZone(UTC).toLocalTime();
        LocalTime sunsetTime = currentSsData.sunset().atZone(UTC).toLocalTime();

        return isBetween(localTime, sunriseTime, sunsetTime);
    }

    private void onTimesRefresh() {
        sunriseSunsetTimes.getCurrentSunriseSunset(coordinates)
                .whenCompleteAsync((sunriseSunsetData, e) -> {
                    if (e != null) {
                        logger.error("Unable to get current SS times, will retry in 5 minutes", e);
                        executor.schedule(Duration.ofMinutes(5), this::onTimesRefresh);
                    } else {
                        logger.debug("Refreshed SS data: {}", sunriseSunsetData);
                        currentSsData = sunriseSunsetData;
                        onRefresh();
                    }
                }, executor);
    }

    private static boolean isBetween(LocalTime checkLocalTime, LocalTime startLocalTime, LocalTime endLocalTime) {
        boolean inBetween = false;
        if (endLocalTime.isAfter(startLocalTime)) {
            if (startLocalTime.isBefore(checkLocalTime) && endLocalTime.isAfter(checkLocalTime)) {
                inBetween = true;
            }
        } else if (checkLocalTime.isAfter(startLocalTime) || checkLocalTime.isBefore(endLocalTime)) {
            inBetween = true;
        }
        return inBetween;
    }
}
