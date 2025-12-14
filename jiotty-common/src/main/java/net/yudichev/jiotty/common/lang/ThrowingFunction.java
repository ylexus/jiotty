package net.yudichev.jiotty.common.lang;

public interface ThrowingFunction<T, U, E extends Throwable> {

    U apply(T input) throws E;
}