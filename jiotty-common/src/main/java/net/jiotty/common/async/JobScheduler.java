package net.jiotty.common.async;

import net.jiotty.common.lang.Closeable;

public interface JobScheduler {
    Closeable monthly(String jobName, int dayOfMonth, Runnable task);
}
