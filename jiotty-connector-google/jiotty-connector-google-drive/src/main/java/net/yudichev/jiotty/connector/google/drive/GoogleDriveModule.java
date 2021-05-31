package net.yudichev.jiotty.connector.google.drive;

import com.google.api.services.drive.Drive;
import com.google.common.collect.ImmutableSet;
import com.google.inject.TypeLiteral;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.connector.google.common.GoogleApiAuthSettings;
import net.yudichev.jiotty.connector.google.common.impl.BaseGoogleServiceModule;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class GoogleDriveModule extends BaseGoogleServiceModule implements ExposedKeyModule<GoogleDriveClient> {
    private final Set<String> scopes;

    private GoogleDriveModule(GoogleApiAuthSettings settings, Set<String> scopes) {
        super(settings);
        this.scopes = checkNotNull(scopes);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void doConfigure() {
        bind(new TypeLiteral<Set<String>>() {}).annotatedWith(GoogleDriveProvider.Scopes.class).toInstance(scopes);
        bind(Drive.class).annotatedWith(GoogleDriveClientImpl.Dependency.class).toProvider(GoogleDriveProvider.class);
        bind(getExposedKey()).to(boundLifecycleComponent(GoogleDriveClientImpl.class));
        expose(getExposedKey());
    }

    public static final class Builder extends BaseBuilder<ExposedKeyModule<GoogleDriveClient>, Builder> {
        private final ImmutableSet.Builder<String> scopeSetBuilder = ImmutableSet.builder();

        @Override
        public ExposedKeyModule<GoogleDriveClient> build() {
            return new GoogleDriveModule(getSettings(), scopeSetBuilder.build());
        }

        public Builder addScopes(String... scopes) {
            scopeSetBuilder.add(scopes);
            return this;
        }

        @Override
        protected Builder thisBuilder() {
            return this;
        }
    }
}
