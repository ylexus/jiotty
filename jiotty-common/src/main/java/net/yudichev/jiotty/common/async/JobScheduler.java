package net.yudichev.jiotty.common.async;

import net.yudichev.jiotty.common.lang.Closeable;

public interface JobScheduler {
    Closeable monthly(String jobName, int dayOfMonth, Runnable task);
}
