package net.yudichev.jiotty.connector.rpigpio;

import com.pi4j.context.Context;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

public final class Pi4jContextModule extends BaseLifecycleComponentModule implements ExposedKeyModule<Context> {
    private Pi4jContextModule() {
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        bind(getExposedKey()).toProvider(registerLifecycleComponent(Pi4jContextProvider.class));
        expose(getExposedKey());
    }

    public static class Builder implements TypedBuilder<ExposedKeyModule<Context>> {
        @Override
        public ExposedKeyModule<Context> build() {
            return new Pi4jContextModule();
        }
    }
}
