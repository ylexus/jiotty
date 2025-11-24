package net.yudichev.jiotty.connector.tesla.wallconnector;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TeslaWallConnectorModule extends BaseLifecycleComponentModule implements ExposedKeyModule<TeslaWallConnector> {
    private final BindingSpec<String> hostAddressSpec;

    private TeslaWallConnectorModule(BindingSpec<String> hostAddressSpec) {
        this.hostAddressSpec = checkNotNull(hostAddressSpec);
    }

    @Override
    protected void configure() {
        hostAddressSpec.bind(String.class).annotatedWith(TeslaWallConnectorImpl.HostAddress.class).installedBy(this::installLifecycleComponentModule);
        bind(getExposedKey()).to(registerLifecycleComponent(TeslaWallConnectorImpl.class));
        expose(getExposedKey());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<TeslaWallConnector>> {
        private BindingSpec<String> hostAddressSpec;

        public Builder setHostAddress(BindingSpec<String> hostAddressSpec) {
            this.hostAddressSpec = checkNotNull(hostAddressSpec);
            return this;
        }

        @Override
        public ExposedKeyModule<TeslaWallConnector> build() {
            return new TeslaWallConnectorModule(hostAddressSpec);
        }
    }
}
