package net.yudichev.jiotty.logging;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.literally;

public final class PersistingLog4jLevelConfiguratorModule extends BaseLifecycleComponentModule implements ExposedKeyModule<LoggingLevelConfigurator> {
    private final BindingSpec<String> varStoreKeyPrefixSpec;

    private PersistingLog4jLevelConfiguratorModule(BindingSpec<String> varStoreKeyPrefixSpec) {
        this.varStoreKeyPrefixSpec = checkNotNull(varStoreKeyPrefixSpec);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        varStoreKeyPrefixSpec.bind(String.class)
                             .annotatedWith(PersistingLog4jLevelConfigurator.VarStoreKeyPrefix.class)
                             .installedBy(this::installLifecycleComponentModule);
        bind(getExposedKey()).to(registerLifecycleComponent(PersistingLog4jLevelConfigurator.class));
        expose(getExposedKey());
    }

    public static class Builder implements TypedBuilder<ExposedKeyModule<LoggingLevelConfigurator>> {
        private BindingSpec<String> varStoreKeyPrefixSpec = literally("");

        public Builder withVarStoreKeyPrefixSpec(BindingSpec<String> varStoreKeyPrefixSpec) {
            this.varStoreKeyPrefixSpec = checkNotNull(varStoreKeyPrefixSpec);
            return this;
        }

        @Override
        public ExposedKeyModule<LoggingLevelConfigurator> build() {
            return new PersistingLog4jLevelConfiguratorModule(varStoreKeyPrefixSpec);
        }
    }
}
