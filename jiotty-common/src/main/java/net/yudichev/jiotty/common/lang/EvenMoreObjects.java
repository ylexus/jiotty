package net.yudichev.jiotty.common.lang;

import javax.annotation.Nullable;
import java.util.function.Function;

public final class EvenMoreObjects {
    private EvenMoreObjects() {
    }

    @SuppressWarnings("ReturnOfNull")
    public static <T, U> U mapIfNotNull(@Nullable T value, Function<? super T, ? extends U> mapper) {
        return value == null ? null : mapper.apply(value);
    }
}
