package net.yudichev.jiotty.common.lang;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class CompositeRunnable implements Runnable {
    private final List<Runnable> delegates = new CopyOnWriteArrayList<>();

    @Override
    public void run() {
        CompositeException.runForAll(delegates, Runnable::run);
    }

    public Closeable add(Runnable runnable) {
        delegates.add(runnable);
        return Closeable.idempotent(() -> delegates.remove(runnable));
    }
}
