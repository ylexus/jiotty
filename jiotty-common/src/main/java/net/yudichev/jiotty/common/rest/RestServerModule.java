package net.yudichev.jiotty.common.rest;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;

public final class RestServerModule extends BaseLifecycleComponentModule implements ExposedKeyModule<RestServer> {
    @Override
    protected void configure() {
        bind(getExposedKey()).to(registerLifecycleComponent(JavalinRestServer.class));
        expose(getExposedKey());
    }
}
