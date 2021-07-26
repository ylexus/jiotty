package net.yudichev.jiotty.connector.ip;

import net.yudichev.jiotty.common.lang.Closeable;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public interface HostMonitor {
    Closeable addListener(Consumer<Status> statusConsumer, Executor executor);

    default Closeable addListener(Consumer<Status> statusConsumer) {
        return addListener(statusConsumer, ForkJoinPool.commonPool());
    }

    enum Status {
        UP, DOWN
    }
}
