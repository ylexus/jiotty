package net.yudichev.jiotty.appliance;

import com.google.inject.Key;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.inject.SpecifiedAnnotation;

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

        bind(exposedKey).to(implKey);
        expose(exposedKey);
    }

    protected abstract Key<? extends Appliance> configureDependencies();
}
