package net.yudichev.jiotty.common.lang;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Observable<T> extends Supplier<T> {
    Closeable subscribe(Consumer<? super T> listener);

    int subscriberCount();
}
