package net.yudichev.jiotty.connector.ir;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public final class BroadLinkIrDeviceModule extends BaseLifecycleComponentModule implements ExposedKeyModule<IrDevice> {
    private final String host;
    private final String macAddress;

    private BroadLinkIrDeviceModule(String host, String macAddress) {
        this.host = checkNotNull(host);
        this.macAddress = checkNotNull(macAddress);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(BroadLinkIrDevice.Host.class).to(host);
        bindConstant().annotatedWith(BroadLinkIrDevice.MacAddress.class).to(macAddress);
        bind(getExposedKey()).to(boundLifecycleComponent(BroadLinkIrDevice.class));
        expose(getExposedKey());
    }

    public static class Builder implements TypedBuilder<ExposedKeyModule<IrDevice>> {
        private String host;
        private String macAddress;

        public Builder setHost(String host) {
            this.host = checkNotNull(host);
            return this;
        }

        public Builder setMacAddress(String macAddress) {
            this.macAddress = checkNotNull(macAddress);
            return this;
        }

        @Override
        public ExposedKeyModule<IrDevice> build() {
            return new BroadLinkIrDeviceModule(host, macAddress);
        }
    }
}
