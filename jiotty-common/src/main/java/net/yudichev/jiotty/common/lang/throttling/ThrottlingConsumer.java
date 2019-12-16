package net.yudichev.jiotty.common.lang.throttling;

import net.yudichev.jiotty.common.async.SchedulingExecutor;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class ThrottlingConsumer<T> implements Consumer<T> {
    private final SchedulingExecutor executor;
    private final Duration throttlingDuration;
    private final Consumer<T> delegate;

    @Nullable
    private T pendingValue;
    @SuppressWarnings("BooleanVariableAlwaysNegated") // it reads better this way
    private boolean throttling;

    public ThrottlingConsumer(SchedulingExecutor executor, Duration throttlingDuration, Consumer<T> delegate) {
        this.executor = checkNotNull(executor);
        checkArgument(!throttlingDuration.isNegative(), "throttlingDuration must not be negative, but was %s", throttlingDuration);
        this.throttlingDuration = throttlingDuration;
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public void accept(T t) {
        executor.execute(() -> {
            pendingValue = t;
            if (!throttling) {
                deliverValue();
            }
        });
    }

    private void deliverValue() {
        delegate.accept(pendingValue);
        pendingValue = null;

        executor.schedule(throttlingDuration, this::onTimer);
        throttling = true;
    }

    private void onTimer() {
        if (pendingValue != null) {
            deliverValue();
        } else {
            throttling = false;
        }
    }
}
