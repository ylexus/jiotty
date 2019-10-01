package net.jiotty.common.time;

import net.jiotty.common.inject.BaseLifecycleComponentModule;

public final class TimeModule extends BaseLifecycleComponentModule {
    @Override
    protected void configure() {
        bind(CurrentDateTimeProvider.class).to(TimeProvider.class);
        expose(CurrentDateTimeProvider.class);
    }
}
