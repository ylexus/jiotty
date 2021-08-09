package net.yudichev.jiotty.connector.google.drive;

import com.google.api.services.drive.Drive;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.connector.google.common.GoogleAuthorization;
import net.yudichev.jiotty.connector.google.common.impl.BaseGoogleServiceModule;

public final class GoogleDriveModule extends BaseGoogleServiceModule implements ExposedKeyModule<GoogleDriveClient> {
    private GoogleDriveModule(BindingSpec<GoogleAuthorization> googleAuthorizationSpec) {
        super(googleAuthorizationSpec);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void doConfigure() {
        bind(Drive.class).annotatedWith(GoogleDriveClientImpl.Dependency.class).toProvider(GoogleDriveProvider.class);
        bind(getExposedKey()).to(registerLifecycleComponent(GoogleDriveClientImpl.class));
        expose(getExposedKey());
    }

    public static final class Builder extends BaseBuilder<GoogleDriveClient, Builder> {
        @Override
        public ExposedKeyModule<GoogleDriveClient> build() {
            return new GoogleDriveModule(getAuthorizationSpec());
        }

        @Override
        protected Builder thisBuilder() {
            return this;
        }
    }
}
