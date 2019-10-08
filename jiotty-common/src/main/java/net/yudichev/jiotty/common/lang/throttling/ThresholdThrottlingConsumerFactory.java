package net.yudichev.jiotty.common.lang.throttling;

import java.time.Duration;
import java.util.function.Consumer;

public interface ThresholdThrottlingConsumerFactory<T> {
    Consumer<T> create(int threshold, Duration throttlingDuration, Consumer<T> delegate);
}
