package net.yudichev.jiotty.connector.nest;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NestModule extends BaseLifecycleComponentModule implements ExposedKeyModule<NestThermostat> {
    private final String accessToken;
    private final String deviceId;

    private NestModule(String accessToken, String deviceId) {
        this.accessToken = checkNotNull(accessToken);
        this.deviceId = checkNotNull(deviceId);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(NestThermostatImpl.AccessToken.class).to(accessToken);
        bindConstant().annotatedWith(NestThermostatImpl.DeviceId.class).to(deviceId);
        bind(getExposedKey()).to(boundLifecycleComponent(NestThermostatImpl.class));
        expose(getExposedKey());
    }

    public static class Builder implements TypedBuilder<ExposedKeyModule<NestThermostat>> {
        private String accessToken;
        private String deviceId;

        public Builder setAccessToken(String accessToken) {
            this.accessToken = checkNotNull(accessToken);
            return this;
        }

        public Builder setDeviceId(String deviceId) {
            this.deviceId = checkNotNull(deviceId);
            return this;
        }

        @Override
        public ExposedKeyModule<NestThermostat> build() {
            return new NestModule(accessToken, deviceId);
        }
    }
}
