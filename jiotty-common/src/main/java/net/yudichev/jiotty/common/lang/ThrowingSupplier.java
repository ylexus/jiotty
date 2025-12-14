package net.yudichev.jiotty.common.lang;

public interface ThrowingSupplier<T, E extends Throwable> {

    T get() throws E;
}