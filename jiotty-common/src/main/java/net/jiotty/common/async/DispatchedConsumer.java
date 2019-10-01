package net.jiotty.common.async;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DispatchedConsumer<T> implements Consumer<T> {
    private final Consumer<T> delegate;
    private final Executor executor;

    public DispatchedConsumer(Consumer<T> delegate, Executor executor) {
        this.delegate = checkNotNull(delegate);
        this.executor = checkNotNull(executor);
    }

    @Override
    public void accept(T value) {
        executor.execute(() -> delegate.accept(value));
    }
}
