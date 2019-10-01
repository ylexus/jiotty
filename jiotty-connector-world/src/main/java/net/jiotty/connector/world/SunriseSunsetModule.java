package net.jiotty.connector.world;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import net.jiotty.common.inject.BaseLifecycleComponentModule;

public final class SunriseSunsetModule extends BaseLifecycleComponentModule {
    @Override
    protected void configure() {
        bind(SunriseSunsetTimes.class).to(SunriseSunsetTimesImpl.class);

        install(new FactoryModuleBuilder()
                .implement(SunriseSunsetService.class, SunriseSunsetServiceImpl.class)
                .build(SunriseSunsetServiceFactory.class));

        expose(SunriseSunsetTimes.class);
        expose(SunriseSunsetServiceFactory.class);
    }
}
