package net.yudichev.jiotty.common.async;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;

public final class ExecutorModule extends BaseLifecycleComponentModule implements ExposedKeyModule<ExecutorFactory> {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(SchedulingExecutor.class, SingleThreadedSchedulingExecutor.class)
                .build(getExposedKey()));
        expose(getExposedKey());
    }
}
