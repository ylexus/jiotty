package net.jiotty.connector.nest;

import net.jiotty.common.inject.BaseLifecycleComponentModule;
import net.jiotty.common.inject.ExposedKeyModule;

import static com.google.common.base.Preconditions.checkNotNull;

// TODO use builder pattern in all jiotty modules
public final class NestModule extends BaseLifecycleComponentModule implements ExposedKeyModule<NestThermostat> {
    private final String accessToken;
    private final String deviceId;

    public NestModule(String accessToken, String deviceId) {
        this.accessToken = checkNotNull(accessToken);
        this.deviceId = checkNotNull(deviceId);
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(NestThermostatImpl.AccessToken.class).to(accessToken);
        bindConstant().annotatedWith(NestThermostatImpl.DeviceId.class).to(deviceId);
        bind(getExposedKey()).to(boundLifecycleComponent(NestThermostatImpl.class));
        expose(getExposedKey());
    }
}
