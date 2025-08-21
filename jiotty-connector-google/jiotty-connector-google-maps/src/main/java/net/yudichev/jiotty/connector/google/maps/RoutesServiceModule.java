package net.yudichev.jiotty.connector.google.maps;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RoutesServiceModule extends BaseLifecycleComponentModule implements ExposedKeyModule<RoutesService> {
    private final BindingSpec<String> apiKeySpec;

    public RoutesServiceModule(BindingSpec<String> apiKeySpec) {
        this.apiKeySpec = checkNotNull(apiKeySpec);
    }

    @Override
    protected void configure() {
        apiKeySpec.bind(String.class).annotatedWith(Bindings.ApiKey.class).installedBy(this::installLifecycleComponentModule);

        bind(getExposedKey()).to(RoutesServiceImpl.class);
        expose(getExposedKey());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<RoutesService>> {
        private BindingSpec<String> apiKeySpec;

        public Builder setApiKey(BindingSpec<String> apiKeySpec) {
            this.apiKeySpec = checkNotNull(apiKeySpec);
            return this;
        }


        @Override
        public ExposedKeyModule<RoutesService> build() {
            return new RoutesServiceModule(apiKeySpec);
        }
    }
}
