package net.jiotty.common.lang;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.jiotty.common.lang.Closeable.idempotent;
import static net.jiotty.common.lang.CompositeException.runForAll;

public final class CompositeRunnable implements Runnable {
    private final List<Runnable> delegates = new CopyOnWriteArrayList<>();

    @Override
    public void run() {
        runForAll(delegates, Runnable::run);
    }

    public Closeable add(Runnable runnable) {
        delegates.add(runnable);
        return idempotent(() -> delegates.remove(runnable));
    }
}
