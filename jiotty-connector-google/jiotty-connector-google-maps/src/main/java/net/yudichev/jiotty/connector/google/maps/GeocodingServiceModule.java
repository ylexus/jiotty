package net.yudichev.jiotty.connector.google.maps;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public final class GeocodingServiceModule extends BaseLifecycleComponentModule implements ExposedKeyModule<GeocodingService> {
    private final BindingSpec<String> apiKeySpec;

    public GeocodingServiceModule(BindingSpec<String> apiKeySpec) {
        this.apiKeySpec = checkNotNull(apiKeySpec);
    }

    @Override
    protected void configure() {
        apiKeySpec.bind(String.class).annotatedWith(Bindings.ApiKey.class).installedBy(this::installLifecycleComponentModule);

        bind(getExposedKey()).to(registerLifecycleComponent(GeocodingServiceImpl.class));
        expose(getExposedKey());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<GeocodingService>> {
        private BindingSpec<String> apiKeySpec;

        public Builder setApiKey(BindingSpec<String> apiKeySpec) {
            this.apiKeySpec = checkNotNull(apiKeySpec);
            return this;
        }


        @Override
        public ExposedKeyModule<GeocodingService> build() {
            return new GeocodingServiceModule(apiKeySpec);
        }
    }
}
