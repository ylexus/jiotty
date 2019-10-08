package net.yudichev.jiotty.common.lang.throttling;

import com.google.inject.assistedinject.Assisted;
import net.yudichev.jiotty.common.time.CurrentDateTimeProvider;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

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
        thresholdGatedConsumer = ThresholdGatedConsumer.thresholdGated(threshold, delegate);
        this.throttlingDuration = checkNotNull(throttlingDuration);
    }

    @Override
    public void accept(T t) {
        Instant now = currentDateTimeProvider.currentInstant();
        if (nextTimeout != null && now.isAfter(nextTimeout)) {
            thresholdGatedConsumer.reset();
        }
        nextTimeout = now.plus(throttlingDuration);
        thresholdGatedConsumer.accept(t);
    }
}
