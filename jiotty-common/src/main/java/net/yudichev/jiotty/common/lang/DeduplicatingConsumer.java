package net.yudichev.jiotty.common.lang;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DeduplicatingConsumer<T> implements Consumer<T> {
    private final EqualityComparator<T> equalityComparator;
    private final Consumer<T> delegate;

    private final AtomicReference<T> lastValue = new AtomicReference<>();

    public DeduplicatingConsumer(EqualityComparator<T> equalityComparator, Consumer<T> delegate) {
        this.equalityComparator = checkNotNull(equalityComparator);
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public void accept(T t) {
        T previousValue = lastValue.getAndSet(t);
        if (previousValue == null || !equalityComparator.areEqual(previousValue, t)) {
            delegate.accept(t);
        }
    }
}
