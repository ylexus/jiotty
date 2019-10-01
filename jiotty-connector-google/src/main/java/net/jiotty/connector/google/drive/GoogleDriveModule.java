package net.jiotty.connector.google.drive;

import com.google.api.services.drive.Drive;
import net.jiotty.common.inject.ExposedKeyModule;
import net.jiotty.connector.google.GoogleApiSettings;
import net.jiotty.connector.google.impl.BaseGoogleServiceModule;

import javax.inject.Singleton;

public final class GoogleDriveModule extends BaseGoogleServiceModule implements ExposedKeyModule<GoogleDriveClient> {

    private GoogleDriveModule(GoogleApiSettings settings) {
        super(settings);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void doConfigure() {
        bind(Drive.class).annotatedWith(GoogleDriveClientImpl.Dependency.class).toProvider(GoogleDriveProvider.class).in(Singleton.class);
        bind(getExposedKey()).to(boundLifecycleComponent(GoogleDriveClientImpl.class));
        expose(getExposedKey());
    }

    public static final class Builder extends BaseBuilder<ExposedKeyModule<GoogleDriveClient>, Builder> {
        @Override
        public ExposedKeyModule<GoogleDriveClient> build() {
            return new GoogleDriveModule(getSettings());
        }

        @Override
        protected Builder thisBuilder() {
            return this;
        }
    }
}
