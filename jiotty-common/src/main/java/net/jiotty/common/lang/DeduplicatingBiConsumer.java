package net.jiotty.common.lang;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DeduplicatingBiConsumer<T, U> implements BiConsumer<T, U> {
    private final BiConsumer<T, U> delegate;
    @Nullable
    private T lastValue1;
    @Nullable
    private U lastValue2;

    public DeduplicatingBiConsumer(BiConsumer<T, U> delegate) {
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public void accept(T value1, U value2) {
        if (!value1.equals(lastValue1) || !value2.equals(lastValue2)) {
            delegate.accept(value1, value2);
            lastValue1 = value1;
            lastValue2 = value2;
        }
    }
}
