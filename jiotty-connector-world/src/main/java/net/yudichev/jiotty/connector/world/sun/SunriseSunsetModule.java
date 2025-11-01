package net.yudichev.jiotty.connector.world.sun;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

public final class SunriseSunsetModule extends BaseLifecycleComponentModule implements ExposedKeyModule<SunriseSunsetServiceFactory> {
    private SunriseSunsetModule() {
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                        .implement(SunriseSunsetService.class, SunriseSunsetServiceImpl.class)
                        .build(getExposedKey()));
        expose(getExposedKey());
    }

    public static class Builder implements TypedBuilder<ExposedKeyModule<SunriseSunsetServiceFactory>> {
        @Override
        public ExposedKeyModule<SunriseSunsetServiceFactory> build() {
            return new SunriseSunsetModule();
        }
    }
}
