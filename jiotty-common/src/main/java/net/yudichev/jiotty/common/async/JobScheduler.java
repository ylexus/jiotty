package net.yudichev.jiotty.common.async;

import net.yudichev.jiotty.common.lang.Closeable;

import java.time.LocalTime;

public interface JobScheduler {
    Closeable monthly(String jobName, int dayOfMonth, Runnable task);

    Closeable monthly(Scheduler scheduler, String jobName, int dayOfMonth, Runnable task);

    Closeable daily(String jobName, LocalTime time, Runnable task);

    Closeable daily(Scheduler scheduler, String jobName, LocalTime time, Runnable task);
}
