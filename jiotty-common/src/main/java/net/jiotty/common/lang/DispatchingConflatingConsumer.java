package net.jiotty.common.lang;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DispatchingConflatingConsumer<T> implements Consumer<T> {
    private final Executor executor;
    private final Consumer<Supplier<T>> delegate;

    private final ConflatingInbox<T> inbox = new ConflatingInbox<>();

    public DispatchingConflatingConsumer(Executor executor, Consumer<Supplier<T>> delegate) {
        this.executor = checkNotNull(executor);
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public void accept(T t) {
        if (inbox.add(t)) {
            executor.execute(this::processQueue);
        }
    }

    private void processQueue() {
        delegate.accept(() -> inbox.get().orElseThrow(IllegalStateException::new));
    }
}
