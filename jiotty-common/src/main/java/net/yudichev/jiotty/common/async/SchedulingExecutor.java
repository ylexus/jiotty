package net.yudichev.jiotty.common.async;

import net.yudichev.jiotty.common.lang.Closeable;

public interface SchedulingExecutor extends TaskExecutor, Closeable, Scheduler {
}
