package net.yudichev.jiotty.common.async;

import net.yudichev.jiotty.common.lang.Closeable;

import java.util.concurrent.Executor;

public interface SchedulingExecutor extends Executor, Closeable, Scheduler {
}
