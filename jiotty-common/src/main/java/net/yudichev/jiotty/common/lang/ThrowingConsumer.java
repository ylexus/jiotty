package net.yudichev.jiotty.common.lang;

public interface ThrowingConsumer<T, E extends Throwable> {

    void accept(T input) throws E;
}