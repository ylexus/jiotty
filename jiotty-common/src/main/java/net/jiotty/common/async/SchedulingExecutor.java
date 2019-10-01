package net.jiotty.common.async;

import net.jiotty.common.lang.Closeable;

import java.util.concurrent.Executor;

public interface SchedulingExecutor extends Executor, Closeable, Scheduler {
}
