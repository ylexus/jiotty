package net.jiotty.connector.ir;

import net.jiotty.common.inject.BaseLifecycleComponentModule;
import net.jiotty.common.inject.ExposedKeyModule;

import static com.google.common.base.Preconditions.checkNotNull;

public final class IrDeviceModule extends BaseLifecycleComponentModule implements ExposedKeyModule<IrDevice> {
    private final String host;
    private final String macAddress;

    public IrDeviceModule(String host, String macAddress) {
        this.host = checkNotNull(host);
        this.macAddress = checkNotNull(macAddress);
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(BroadLinkIrDevice.Host.class).to(host);
        bindConstant().annotatedWith(BroadLinkIrDevice.MacAddress.class).to(macAddress);
        bind(getExposedKey()).to(boundLifecycleComponent(BroadLinkIrDevice.class));
        expose(getExposedKey());
    }
}
