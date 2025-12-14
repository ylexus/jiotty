package net.yudichev.jiotty.world.homelocation;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;

//TODO:commerce this needs to get location using client device's location services or by searching address (via google API)
public final class HomeLocationModule extends BaseLifecycleComponentModule implements ExposedKeyModule<HomeLocationService> {
    @Override
    protected void configure() {
        bind(getExposedKey()).to(registerLifecycleComponent(HomeLocationServiceImpl.class));
        expose(getExposedKey());
    }
}
