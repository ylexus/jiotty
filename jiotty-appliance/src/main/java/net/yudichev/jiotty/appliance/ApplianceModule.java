package net.yudichev.jiotty.appliance;

import com.google.inject.Key;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.inject.SpecifiedAnnotation;

import javax.inject.Singleton;
import java.util.concurrent.Executor;

public abstract class ApplianceModule extends BaseLifecycleComponentModule implements ExposedKeyModule<Appliance> {
    private final Key<Appliance> exposedKey;

    protected ApplianceModule(SpecifiedAnnotation targetAnnotation) {
        exposedKey = targetAnnotation.specify(Appliance.class);
    }

    @Override
    public final Key<Appliance> getExposedKey() {
        return exposedKey;
    }

    @Override
    protected final void configure() {
        Key<? extends Appliance> implKey = configureDependencies();

        bind(Appliance.class).annotatedWith(ConflatingAsyncAppliance.Delegate.class).to(implKey);
        Key<SchedulingExecutor> executorKey = Key.get(SchedulingExecutor.class, Bindings.ApplianceExecutor.class);
        bind(executorKey).toProvider(boundLifecycleComponent(ApplianceExecutorProvider.class));
        bind(Executor.class).annotatedWith(Bindings.ApplianceExecutor.class).to(executorKey);
        bind(ConflatingAsyncAppliance.class).in(Singleton.class);
        bind(exposedKey).to(ConflatingAsyncAppliance.class);
        expose(exposedKey);
    }

    protected abstract Key<? extends Appliance> configureDependencies();
}
