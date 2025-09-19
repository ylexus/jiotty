package net.yudichev.jiotty.common.security;

import com.google.inject.Key;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.inject.HasWithAnnotation;
import net.yudichev.jiotty.common.inject.SpecifiedAnnotation;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OAuth2TokenManagerModule extends BaseLifecycleComponentModule implements ExposedKeyModule<OAuth2TokenManager> {
    private final BindingSpec<String> clientIdSpec;
    private final BindingSpec<String> clientSecretSpec;
    private final BindingSpec<String> apiNameSpec;
    private final BindingSpec<String> authBaseUrlSpec;
    private final Key<OAuth2TokenManager> exposedKey;

    private OAuth2TokenManagerModule(BindingSpec<String> clientIdSpec,
                                     BindingSpec<String> clientSecretSpec,
                                     BindingSpec<String> apiNameSpec,
                                     BindingSpec<String> authBaseUrlSpec,
                                     SpecifiedAnnotation specifiedAnnotation) {
        this.clientIdSpec = checkNotNull(clientIdSpec);
        this.clientSecretSpec = checkNotNull(clientSecretSpec);
        this.apiNameSpec = checkNotNull(apiNameSpec);
        this.authBaseUrlSpec = checkNotNull(authBaseUrlSpec);
        exposedKey = specifiedAnnotation.specify(OAuth2TokenManager.class);
    }

    @Override
    public Key<OAuth2TokenManager> getExposedKey() {
        return exposedKey;
    }

    @Override
    protected void configure() {
        clientIdSpec.bind(String.class)
                    .annotatedWith(OAuth2TokenManagerImpl.ClientID.class)
                    .installedBy(this::installLifecycleComponentModule);
        clientSecretSpec.bind(String.class)
                        .annotatedWith(OAuth2TokenManagerImpl.ClientSecret.class)
                        .installedBy(this::installLifecycleComponentModule);
        apiNameSpec.bind(String.class)
                   .annotatedWith(OAuth2TokenManagerImpl.ApiName.class)
                   .installedBy(this::installLifecycleComponentModule);
        authBaseUrlSpec.bind(String.class)
                       .annotatedWith(OAuth2TokenManagerImpl.AuthBaseUrl.class)
                       .installedBy(this::installLifecycleComponentModule);

        bind(exposedKey).to(registerLifecycleComponent(OAuth2TokenManagerImpl.class));
        expose(exposedKey);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<OAuth2TokenManager>>, HasWithAnnotation {
        private BindingSpec<String> clientIdSpec;
        private BindingSpec<String> clientSecretSpec;
        private BindingSpec<String> apiNameSpec;
        private BindingSpec<String> authBaseUrlSpec;
        private SpecifiedAnnotation specifiedAnnotation = SpecifiedAnnotation.forNoAnnotation();

        public Builder setClientSecret(BindingSpec<String> clientSecretSpec) {
            this.clientSecretSpec = clientSecretSpec;
            return this;
        }

        public Builder setClientId(BindingSpec<String> clientIdSpec) {
            this.clientIdSpec = clientIdSpec;
            return this;
        }

        public Builder setApiName(BindingSpec<String> apiNameSpec) {
            this.apiNameSpec = checkNotNull(apiNameSpec);
            return this;
        }

        public Builder setAuthBaseUrl(BindingSpec<String> authBaseUrlSpec) {
            this.authBaseUrlSpec = checkNotNull(authBaseUrlSpec);
            return this;
        }

        @Override
        public Builder withAnnotation(SpecifiedAnnotation specifiedAnnotation) {
            this.specifiedAnnotation = checkNotNull(specifiedAnnotation);
            return this;
        }

        @Override
        public ExposedKeyModule<OAuth2TokenManager> build() {
            return new OAuth2TokenManagerModule(clientIdSpec, clientSecretSpec, apiNameSpec, authBaseUrlSpec, specifiedAnnotation);
        }
    }
}
