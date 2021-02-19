package net.yudichev.jiotty.connector.slide;

import net.yudichev.jiotty.common.async.ExecutorFactory;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.google.common.base.Preconditions.checkNotNull;

final class ExecutorProvider extends BaseLifecycleComponent implements Provider<SchedulingExecutor> {
    private final ExecutorFactory executorFactory;
    private SchedulingExecutor executor;

    @Inject ExecutorProvider(ExecutorFactory executorFactory) {
        this.executorFactory = checkNotNull(executorFactory);
    }

    @Override
    protected void doStart() {
        executor = executorFactory.createSingleThreadedSchedulingExecutor("slide-service");
    }

    @Override
    protected void doStop() {
        executor.close();
    }

    @Override
    public SchedulingExecutor get() {
        return executor;
    }
}
