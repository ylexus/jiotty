package net.jiotty.common.lang.throttling;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

@NotThreadSafe
public final class ThresholdGatedConsumer<T> implements Consumer<T> {
    private final Consumer<T> delegate;

    private final int threshold;
    private int count;

    private ThresholdGatedConsumer(int threshold, Consumer<T> delegate) {
        this.delegate = checkNotNull(delegate);
        this.threshold = threshold;
    }

    public static <T> ThresholdGatedConsumer<T> thresholdGated(int threshold, Consumer<T> delegate) {
        return new ThresholdGatedConsumer<>(threshold, delegate);
    }

    @Override
    public void accept(T value) {
        if (count++ == threshold) {
            delegate.accept(value);
        }
    }

    public void reset() {
        count = 0;
    }
}
