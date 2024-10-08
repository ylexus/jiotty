package net.yudichev.jiotty.common.lang;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseIdempotentCloseable implements Closeable {
    private final AtomicBoolean closed = new AtomicBoolean();

    @Override
    public final void close() {
        if (!closed.getAndSet(true)) {
            doClose();
        }
    }

    public final boolean isClosed() {
        return closed.get();
    }

    protected abstract void doClose();
}
