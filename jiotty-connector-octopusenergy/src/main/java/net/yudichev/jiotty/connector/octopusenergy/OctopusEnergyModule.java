package net.yudichev.jiotty.connector.octopusenergy;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OctopusEnergyModule extends BaseLifecycleComponentModule implements ExposedKeyModule<OctopusEnergy> {
    private final BindingSpec<String> apiKeySpec;
    private final BindingSpec<String> accountIdSpec;

    private OctopusEnergyModule(BindingSpec<String> apiKeySpec, BindingSpec<String> accountIdSpec) {
        this.apiKeySpec = checkNotNull(apiKeySpec);
        this.accountIdSpec = checkNotNull(accountIdSpec);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        apiKeySpec.bind(String.class)
                  .annotatedWith(OctopusEnergyImpl.ApiKey.class)
                  .installedBy(this::installLifecycleComponentModule);
        accountIdSpec.bind(String.class)
                     .annotatedWith(OctopusEnergyImpl.AccountId.class)
                     .installedBy(this::installLifecycleComponentModule);
        bind(getExposedKey()).to(registerLifecycleComponent(OctopusEnergyImpl.class));
        expose(getExposedKey());
    }

    public static class Builder implements TypedBuilder<ExposedKeyModule<OctopusEnergy>> {
        private BindingSpec<String> apiKeySpec;
        private BindingSpec<String> accountIdSpec;

        public Builder setApiKey(BindingSpec<String> apiKeySpec) {
            this.apiKeySpec = checkNotNull(apiKeySpec);
            return this;
        }

        public Builder setAccountId(BindingSpec<String> accountIdSpec) {
            this.accountIdSpec = checkNotNull(accountIdSpec);
            return this;
        }

        @Override
        public ExposedKeyModule<OctopusEnergy> build() {
            return new OctopusEnergyModule(apiKeySpec, accountIdSpec);
        }
    }
}
