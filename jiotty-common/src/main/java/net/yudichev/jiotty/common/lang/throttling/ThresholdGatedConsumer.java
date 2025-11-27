package net.yudichev.jiotty.common.lang.throttling;

import java.util.function.Consumer;

interface ThresholdGatedConsumer<T> extends Consumer<T> {
    void reset();

    static <T> ThresholdGatedConsumer<T> thresholdGated(int threshold, Consumer<T> delegate) {
        return new ThresholdGatedConsumerImpl<>(threshold, delegate);
    }
}
