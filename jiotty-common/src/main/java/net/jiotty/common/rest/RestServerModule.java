package net.jiotty.common.rest;

import net.jiotty.common.inject.BaseLifecycleComponentModule;
import net.jiotty.common.inject.ExposedKeyModule;

public final class RestServerModule extends BaseLifecycleComponentModule implements ExposedKeyModule<RestServer> {
    @Override
    protected void configure() {
        bind(getExposedKey()).to(boundLifecycleComponent(SparkRestServer.class));
        expose(getExposedKey());
    }
}
