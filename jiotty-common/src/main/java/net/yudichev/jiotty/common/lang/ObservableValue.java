package net.yudichev.jiotty.common.lang;

import java.util.function.Consumer;

public interface ObservableValue<T> extends Consumer<T>, Observable<T> {

    static <T> ObservableValue<T> concurrent(T initialValue) {
        return new ConcurrentObservableValue<>(initialValue);
    }

    static <T> ObservableValue<T> simple(T initialValue) {
        return new SimpleObservableValue<>(initialValue);
    }

    void setNotificationsSuppressed(boolean suppressed);
}
