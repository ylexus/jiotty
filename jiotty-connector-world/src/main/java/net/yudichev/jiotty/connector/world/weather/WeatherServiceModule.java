package net.yudichev.jiotty.connector.world.weather;

import com.google.inject.Key;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.inject.HasWithAnnotation;
import net.yudichev.jiotty.common.inject.SpecifiedAnnotation;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public final class WeatherServiceModule extends BaseLifecycleComponentModule implements ExposedKeyModule<WeatherService> {
    private final BindingSpec<String> apiKeySpec;
    private final Key<WeatherService> exposedKey;

    private WeatherServiceModule(SpecifiedAnnotation specifiedAnnotation, BindingSpec<String> apiKeySpec) {
        exposedKey = specifiedAnnotation.specify(ExposedKeyModule.super.getExposedKey().getTypeLiteral());
        this.apiKeySpec = checkNotNull(apiKeySpec);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Key<WeatherService> getExposedKey() {
        return exposedKey;
    }

    @Override
    protected void configure() {
        apiKeySpec.bind(String.class)
                  .annotatedWith(WeatherServiceImpl.ApiKey.class)
                  .installedBy(this::installLifecycleComponentModule);

        bind(exposedKey).to(registerLifecycleComponent(WeatherServiceImpl.class));
        expose(exposedKey);
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<WeatherService>>, HasWithAnnotation {
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
        public ExposedKeyModule<WeatherService> build() {
            return new WeatherServiceModule(specifiedAnnotation, apiKeySpec);
        }
    }
}
