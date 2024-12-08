package net.yudichev.jiotty.common.async;

public final class ExecutorFactoryImpl implements ExecutorFactory {
    @Override
    public SchedulingExecutor createSingleThreadedSchedulingExecutor(String threadNameBase) {
        return new SingleThreadedSchedulingExecutor(threadNameBase);
    }
}
