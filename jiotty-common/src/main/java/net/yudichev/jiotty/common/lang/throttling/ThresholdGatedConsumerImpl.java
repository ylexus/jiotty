package net.yudichev.jiotty.common.lang.throttling;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

@NotThreadSafe
final class ThresholdGatedConsumerImpl<T> implements ThresholdGatedConsumer<T> {
    private final Consumer<T> delegate;

    private final int threshold;
    private int count;

    ThresholdGatedConsumerImpl(int threshold, Consumer<T> delegate) {
        this.delegate = checkNotNull(delegate);
        this.threshold = threshold;
    }

    @Override
    public void accept(T t) {
        if (count == threshold) {
            delegate.accept(t);
        }
        count++;
    }

    @Override
    public void reset() {
        count = 0;
    }
}
