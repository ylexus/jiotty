package net.yudichev.jiotty.connector.google.maps;

import com.google.inject.Key;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.inject.HasWithAnnotation;
import net.yudichev.jiotty.common.inject.SpecifiedAnnotation;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public final class GeocodingServiceModule extends BaseLifecycleComponentModule implements ExposedKeyModule<GeocodingService> {
    private final BindingSpec<String> apiKeySpec;
    private final Key<GeocodingService> exposedKey;

    public GeocodingServiceModule(SpecifiedAnnotation specifiedAnnotation, BindingSpec<String> apiKeySpec) {
        exposedKey = specifiedAnnotation.specify(ExposedKeyModule.super.getExposedKey().getTypeLiteral());
        this.apiKeySpec = checkNotNull(apiKeySpec);
    }

    @Override
    public Key<GeocodingService> getExposedKey() {
        return exposedKey;
    }

    @Override
    protected void configure() {
        apiKeySpec.bind(String.class).annotatedWith(Bindings.ApiKey.class).installedBy(this::installLifecycleComponentModule);

        bind(exposedKey).to(registerLifecycleComponent(GeocodingServiceImpl.class));
        expose(exposedKey);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<GeocodingService>>, HasWithAnnotation {
        private BindingSpec<String> apiKeySpec;
        private SpecifiedAnnotation specifiedAnnotation = SpecifiedAnnotation.forNoAnnotation();

        public Builder setApiKey(BindingSpec<String> apiKeySpec) {
            this.apiKeySpec = checkNotNull(apiKeySpec);
            return this;
        }

        @Override
        public Builder withAnnotation(SpecifiedAnnotation specifiedAnnotation) {
            this.specifiedAnnotation = checkNotNull(specifiedAnnotation);
            return this;
        }

        @Override
        public ExposedKeyModule<GeocodingService> build() {
            return new GeocodingServiceModule(specifiedAnnotation, apiKeySpec);
        }
    }
}
