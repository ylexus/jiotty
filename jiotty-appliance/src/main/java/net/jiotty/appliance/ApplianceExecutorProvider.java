package net.jiotty.appliance;

import net.jiotty.common.async.ExecutorFactory;
import net.jiotty.common.async.SchedulingExecutor;
import net.jiotty.common.inject.BaseLifecycleComponent;

import javax.inject.Inject;
import javax.inject.Provider;

final class ApplianceExecutorProvider extends BaseLifecycleComponent implements Provider<SchedulingExecutor> {
    private final SchedulingExecutor executor;

    @Inject
    ApplianceExecutorProvider(ExecutorFactory executorFactory) {
        executor = executorFactory.createSingleThreadedSchedulingExecutor("appliance");
    }

    @Override
    public SchedulingExecutor get() {
        return executor;
    }

    @Override
    protected void doStop() {
        executor.close();
    }
}
