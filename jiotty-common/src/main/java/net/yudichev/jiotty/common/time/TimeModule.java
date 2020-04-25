package net.yudichev.jiotty.common.time;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;

public final class TimeModule extends BaseLifecycleComponentModule implements ExposedKeyModule<CurrentDateTimeProvider> {
    @Override
    protected void configure() {
        bind(getExposedKey()).to(TimeProvider.class);
        expose(getExposedKey());
    }
}
