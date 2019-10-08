package net.yudichev.jiotty.common.async;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;

public final class JobSchedulerModule extends BaseLifecycleComponentModule implements ExposedKeyModule<JobScheduler> {
    @Override
    protected void configure() {
        bind(getExposedKey()).to(boundLifecycleComponent(JobSchedulerImpl.class));
        expose(getExposedKey());
    }
}
