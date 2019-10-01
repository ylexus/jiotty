package net.jiotty.common.async;

import net.jiotty.common.inject.BaseLifecycleComponentModule;
import net.jiotty.common.inject.ExposedKeyModule;

public final class JobSchedulerModule extends BaseLifecycleComponentModule implements ExposedKeyModule<JobScheduler> {
    @Override
    protected void configure() {
        bind(getExposedKey()).to(boundLifecycleComponent(JobSchedulerImpl.class));
        expose(getExposedKey());
    }
}
