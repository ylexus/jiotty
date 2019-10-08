package net.yudichev.jiotty.common.async;

import net.yudichev.jiotty.common.lang.Closeable;

import java.time.Duration;

public interface Scheduler {
    Closeable schedule(Duration delay, Runnable command);

    Closeable scheduleAtFixedRate(Duration initialDelay, Duration period, Runnable command);

    default Closeable scheduleAtFixedRate(Duration period, Runnable command) {
        return scheduleAtFixedRate(period, period, command);
    }
}
