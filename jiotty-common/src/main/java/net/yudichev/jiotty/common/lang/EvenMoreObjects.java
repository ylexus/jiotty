package net.yudichev.jiotty.common.lang;

import java.util.function.Function;

public final class EvenMoreObjects {
    private EvenMoreObjects() {
    }

    @SuppressWarnings("ReturnOfNull")
    public static <T, U> U mapIfNotNull(T value, Function<? super T, ? extends U> mapper) {
        return value == null ? null : mapper.apply(value);
    }
}
