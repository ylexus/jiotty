package net.yudichev.jiotty.common.lang;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DeduplicatingConsumer<T> implements Consumer<T> {
    private final EqualityComparator<T> equalityComparator;
    private final Consumer<T> delegate;

    private T lastValue;

    public DeduplicatingConsumer(EqualityComparator<T> equalityComparator, Consumer<T> delegate) {
        this.equalityComparator = checkNotNull(equalityComparator);
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public void accept(T newValue) {
        T previousValue = lastValue;
        lastValue = newValue;
        if (!equalityComparator.areEqual(previousValue, newValue)) {
            delegate.accept(newValue);
        }
    }
}
