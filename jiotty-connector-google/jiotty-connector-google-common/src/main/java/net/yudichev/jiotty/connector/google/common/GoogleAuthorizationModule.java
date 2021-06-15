package net.yudichev.jiotty.connector.google.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.Optionals;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import javax.inject.Singleton;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class GoogleAuthorizationModule extends BaseLifecycleComponentModule implements ExposedKeyModule<GoogleAuthorization> {
    private final GoogleApiAuthSettings settings;
    private final Collection<String> requiredScopes;

    private GoogleAuthorizationModule(GoogleApiAuthSettings settings, Collection<String> requiredScopes) {
        this.settings = checkNotNull(settings);
        this.requiredScopes = ImmutableList.copyOf(requiredScopes);
    }

    @Override
    protected void configure() {
        Optionals
                .ifPresent(settings.authorizationBrowser(), authorizationBrowserSpec -> authorizationBrowserSpec
                        .map(new TypeToken<>() {}, new TypeToken<>() {}, Optional::of)
                        .bind(new TypeLiteral<>() {})
                        .annotatedWith(GoogleAuthorizationProvider.Dependency.class)
                        .installedBy(this::installLifecycleComponentModule))
                .orElse(() -> bind(new TypeLiteral<Optional<AuthorizationBrowser>>() {})
                        .annotatedWith(GoogleAuthorizationProvider.Dependency.class)
                        .toInstance(Optional.empty()));
        settings.credentialsUrl().bind(URL.class)
                .annotatedWith(GoogleAuthorizationProvider.Dependency.class)
                .installedBy(this::installLifecycleComponentModule);
        bind(GoogleApiAuthSettings.class).annotatedWith(GoogleAuthorizationProvider.Dependency.class).toInstance(settings);
        bind(new TypeLiteral<Collection<String>>() {}).annotatedWith(GoogleAuthorizationProvider.Scopes.class).toInstance(requiredScopes);
        bind(getExposedKey()).toProvider(GoogleAuthorizationProvider.class).in(Singleton.class);
        expose(getExposedKey());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<GoogleAuthorization>> {
        private final ImmutableSet.Builder<String> requiredScopesBuilder = ImmutableSet.builder();
        private GoogleApiAuthSettings settings;

        private Builder() {
        }

        public Builder setSettings(GoogleApiAuthSettings settings) {
            this.settings = checkNotNull(settings);
            return this;
        }

        public Builder addRequiredScope(String requiredScope) {
            requiredScopesBuilder.add(requiredScope);
            return this;
        }

        public Builder addRequiredScopes(Iterable<String> requiredScopes) {
            requiredScopesBuilder.addAll(requiredScopes);
            return this;
        }

        public Builder addRequiredScopes(String... requiredScopes) {
            return addRequiredScopes(ImmutableList.copyOf(requiredScopes));
        }


        @Override
        public ExposedKeyModule<GoogleAuthorization> build() {
            return new GoogleAuthorizationModule(settings, requiredScopesBuilder.build());
        }
    }
}
