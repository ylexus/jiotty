package net.jiotty.common.lang.throttling;

import com.google.inject.assistedinject.Assisted;
import net.jiotty.common.time.CurrentDateTimeProvider;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.jiotty.common.lang.throttling.ThresholdGatedConsumer.thresholdGated;

@NotThreadSafe
public final class ThresholdThrottlingConsumer<T> implements Consumer<T> {
    private final CurrentDateTimeProvider currentDateTimeProvider;
    private final ThresholdGatedConsumer<T> thresholdGatedConsumer;
    private final Duration throttlingDuration;
    private Instant nextTimeout;

    @Inject
    ThresholdThrottlingConsumer(CurrentDateTimeProvider currentDateTimeProvider,
                                @Assisted int threshold,
                                @Assisted Duration throttlingDuration,
                                @Assisted Consumer<T> delegate) {
        this.currentDateTimeProvider = checkNotNull(currentDateTimeProvider);
        this.thresholdGatedConsumer = thresholdGated(threshold, delegate);
        this.throttlingDuration = checkNotNull(throttlingDuration);
    }

    @Override
    public void accept(T value) {
        Instant now = currentDateTimeProvider.currentInstant();
        if (nextTimeout != null && now.isAfter(nextTimeout)) {
            thresholdGatedConsumer.reset();
        }
        this.nextTimeout = now.plus(throttlingDuration);
        thresholdGatedConsumer.accept(value);
    }
}
