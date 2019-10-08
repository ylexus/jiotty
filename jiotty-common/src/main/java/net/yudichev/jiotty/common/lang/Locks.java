package net.yudichev.jiotty.common.lang;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

public final class Locks {
    private Locks() {
    }

    public static void inLock(Lock lock, Runnable task) {
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }

    public static <T> T inLock(Lock lock, Supplier<T> supplier) {
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }
}
