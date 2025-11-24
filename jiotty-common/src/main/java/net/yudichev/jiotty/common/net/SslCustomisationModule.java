package net.yudichev.jiotty.common.net;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import javax.inject.Singleton;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SslCustomisationModule extends BaseLifecycleComponentModule implements ExposedKeyModule<SslCustomisation> {
    private final BindingSpec<Path> trustStorePathSpec;
    private final BindingSpec<String> trustStorePasswordSpec;

    private SslCustomisationModule(BindingSpec<Path> trustStorePathSpec, BindingSpec<String> trustStorePasswordSpec) {
        this.trustStorePathSpec = checkNotNull(trustStorePathSpec);
        this.trustStorePasswordSpec = checkNotNull(trustStorePasswordSpec);
    }

    @Override
    protected void configure() {
        trustStorePathSpec.bind(Path.class).annotatedWith(SslCustomisationProvider.Dependency.class).installedBy(this::installLifecycleComponentModule);
        trustStorePasswordSpec.bind(String.class).annotatedWith(SslCustomisationProvider.Dependency.class).installedBy(this::installLifecycleComponentModule);
        bind(getExposedKey()).toProvider(SslCustomisationProvider.class).in(Singleton.class);
        expose(getExposedKey());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<SslCustomisation>> {
        private BindingSpec<Path> trustStorePathSpec;
        private BindingSpec<String> trustStorePasswordSpec;

        public Builder setTrustStorePath(BindingSpec<Path> trustStorePathSpec) {
            this.trustStorePathSpec = checkNotNull(trustStorePathSpec);
            return this;
        }

        public Builder setTrustStorePassword(BindingSpec<String> trustStorePasswordSpec) {
            this.trustStorePasswordSpec = checkNotNull(trustStorePasswordSpec);
            return this;
        }

        @Override
        public ExposedKeyModule<SslCustomisation> build() {
            return new SslCustomisationModule(trustStorePathSpec, trustStorePasswordSpec);
        }
    }
}
