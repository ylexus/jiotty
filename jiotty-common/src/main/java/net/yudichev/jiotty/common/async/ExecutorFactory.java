package net.yudichev.jiotty.common.async;

public interface ExecutorFactory {
    SchedulingExecutor createSingleThreadedSchedulingExecutor(String threadNameBase);
}
