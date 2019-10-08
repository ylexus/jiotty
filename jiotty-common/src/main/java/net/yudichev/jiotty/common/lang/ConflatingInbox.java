package net.yudichev.jiotty.common.lang;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

@ThreadSafe
public final class ConflatingInbox<T> {
    private final AtomicReference<T> pendingValue = new AtomicReference<>();

    public boolean add(T value) {
        return pendingValue.getAndSet(checkNotNull(value)) == null;
    }

    public Optional<T> get() {
        return Optional.ofNullable(pendingValue.getAndSet(null));
    }
}
