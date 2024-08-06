package net.yudichev.jiotty.connector.owntracks;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;
import net.yudichev.jiotty.connector.mqtt.Mqtt;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OwnTracksModule extends BaseLifecycleComponentModule implements ExposedKeyModule<OwnTracks> {
    private final BindingSpec<Mqtt> mqttSpec;

    private OwnTracksModule(BindingSpec<Mqtt> mqttSpec) {
        this.mqttSpec = checkNotNull(mqttSpec);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        mqttSpec.bind(Mqtt.class)
                .annotatedWith(OwnTracksImpl.Dependency.class)
                .installedBy(this::installLifecycleComponentModule);
        bind(getExposedKey()).to(OwnTracksImpl.class);
        expose(getExposedKey());
    }

    public static class Builder implements TypedBuilder<ExposedKeyModule<OwnTracks>> {
        private BindingSpec<Mqtt> mqttSpec = BindingSpec.boundTo(Mqtt.class);

        public Builder withMqtt(BindingSpec<Mqtt> mqttSpec) {
            this.mqttSpec = checkNotNull(mqttSpec);
            return this;
        }

        @Override
        public ExposedKeyModule<OwnTracks> build() {
            return new OwnTracksModule(mqttSpec);
        }
    }
}
