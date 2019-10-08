package net.jiotty.common.lang.throttling;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.function.Consumer;

@NotThreadSafe
interface ThresholdGatedConsumer<T> extends Consumer<T> {
    void reset();

    static <T> ThresholdGatedConsumer<T> thresholdGated(int threshold, Consumer<T> delegate) {
        return new ThresholdGatedConsumerImpl<>(threshold, delegate);
    }
}
