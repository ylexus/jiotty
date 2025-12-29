package net.yudichev.jiotty.common.net;

import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import jakarta.inject.Singleton;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.net.SslCustomisation.TrustStore;

public final class SslCustomisationModule extends BaseLifecycleComponentModule implements ExposedKeyModule<SslCustomisation> {

    private final BindingSpec<TrustStore> certTrustStoreSpec;
    private final BindingSpec<Optional<TrustStore>> clientKeyStoreSpec;

    private SslCustomisationModule(BindingSpec<TrustStore> certTrustStoreSpec,
                                   Optional<BindingSpec<TrustStore>> clientKeyStoreSpec) {
        this.certTrustStoreSpec = checkNotNull(certTrustStoreSpec);
        this.clientKeyStoreSpec = clientKeyStoreSpec.map(spec -> spec.map(new TypeToken<>() {}, new TypeToken<>() {}, Optional::of))
                                                    .orElseGet(() -> BindingSpec.literally(Optional.empty()));

    }

    @Override
    protected void configure() {
        certTrustStoreSpec.bind(TrustStore.class)
                          .annotatedWith(SslCustomisationProvider.CertTrustStore.class)
                          .installedBy(this::installLifecycleComponentModule);

        clientKeyStoreSpec.bind(new TypeLiteral<>() {})
                          .annotatedWith(SslCustomisationProvider.ClientKeyStore.class)
                          .installedBy(this::installLifecycleComponentModule);
        bind(getExposedKey()).toProvider(SslCustomisationProvider.class).in(Singleton.class);
        expose(getExposedKey());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<SslCustomisation>> {
        private BindingSpec<TrustStore> certTrustStoreSpec;
        private BindingSpec<TrustStore> clientKeyStoreSpec;

        public Builder setCertTrustStore(BindingSpec<TrustStore> certTrustStoreSpec) {
            this.certTrustStoreSpec = checkNotNull(certTrustStoreSpec);
            return this;
        }

        public Builder withClientKeyStore(BindingSpec<TrustStore> clientKeyStoreSpec) {
            this.clientKeyStoreSpec = checkNotNull(clientKeyStoreSpec);
            return this;
        }

        @Override
        public ExposedKeyModule<SslCustomisation> build() {
            return new SslCustomisationModule(certTrustStoreSpec, Optional.ofNullable(clientKeyStoreSpec));
        }
    }
}
