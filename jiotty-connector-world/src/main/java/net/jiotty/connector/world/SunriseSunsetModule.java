package net.jiotty.connector.world;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import net.jiotty.common.inject.BaseLifecycleComponentModule;
import net.jiotty.common.inject.ExposedKeyModule;
import net.jiotty.common.lang.TypedBuilder;

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
