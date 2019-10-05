package net.jiotty.appliance;

import com.google.inject.Key;
import net.jiotty.appliance.Bindings.ApplianceExecutor;
import net.jiotty.common.async.SchedulingExecutor;
import net.jiotty.common.inject.BaseLifecycleComponentModule;
import net.jiotty.common.inject.ExposedKeyModule;
import net.jiotty.common.inject.SpecifiedAnnotation;

import javax.inject.Singleton;
import java.util.concurrent.Executor;

public abstract class ApplianceModule extends BaseLifecycleComponentModule implements ExposedKeyModule<Appliance> {
    private final Key<Appliance> exposedKey;

    public ApplianceModule(SpecifiedAnnotation targetAnnotation) {
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
        Key<SchedulingExecutor> executorKey = Key.get(SchedulingExecutor.class, ApplianceExecutor.class);
        bind(executorKey).toProvider(boundLifecycleComponent(ApplianceExecutorProvider.class));
        bind(Executor.class).annotatedWith(ApplianceExecutor.class).to(executorKey);
        bind(ConflatingAsyncAppliance.class).in(Singleton.class);
        bind(exposedKey).to(ConflatingAsyncAppliance.class);
        expose(exposedKey);
    }

    protected abstract Key<? extends Appliance> configureDependencies();
}
