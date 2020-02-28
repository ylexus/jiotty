package net.yudichev.jiotty.common.lang.throttling;

import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.lang.BaseIdempotentCloseable;
import net.yudichev.jiotty.common.lang.Closeable;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.lang.Closeable.closeIfNotNull;
import static net.yudichev.jiotty.common.lang.Closeable.noop;
import static net.yudichev.jiotty.common.lang.Locks.inLock;

public final class ThrottlingConsumer<T> extends BaseIdempotentCloseable implements Consumer<T> {
    private final SchedulingExecutor executor;
    private final Duration throttlingDuration;
    private final Consumer<T> delegate;
    private final Lock stateLock = new ReentrantLock();

    @Nullable
    private T pendingValue;
    private boolean throttling;

    private Closeable throttlingTimerHandle = noop();
    private boolean closed;

    public ThrottlingConsumer(SchedulingExecutor executor, Duration throttlingDuration, Consumer<T> delegate) {
        this.executor = checkNotNull(executor);
        checkArgument(!throttlingDuration.isNegative(), "throttlingDuration must not be negative, but was %s", throttlingDuration);
        this.throttlingDuration = throttlingDuration;
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public void accept(T t) {
        inLock(stateLock, () -> {
            if (!closed) {
                executor.execute(() -> {
                    pendingValue = t;
                    if (!throttling) {
                        deliverValue();
                    }
                });
            }
        });
    }

    @Override
    protected void doClose() {
        inLock(stateLock, () -> {
            closed = true;
            closeIfNotNull(throttlingTimerHandle);
        });
    }

    private void deliverValue() {
        delegate.accept(pendingValue);
        pendingValue = null;

        inLock(stateLock, () -> {
            if (!closed) {
                throttlingTimerHandle = executor.schedule(throttlingDuration, this::onTimer);
                throttling = true;
            }
        });
    }

    private void onTimer() {
        if (pendingValue != null) {
            deliverValue();
        } else {
            throttling = false;
        }
    }
}
