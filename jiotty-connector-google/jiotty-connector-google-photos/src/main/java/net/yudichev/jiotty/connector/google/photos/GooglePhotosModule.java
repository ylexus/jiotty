package net.yudichev.jiotty.connector.google.photos;

import com.google.photos.library.v1.PhotosLibrarySettings;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.connector.google.common.GoogleApiAuthSettings;
import net.yudichev.jiotty.connector.google.common.impl.BaseGoogleServiceModule;

public final class GooglePhotosModule extends BaseGoogleServiceModule implements ExposedKeyModule<GooglePhotosClient> {
    private GooglePhotosModule(GoogleApiAuthSettings settings) {
        super(settings);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void doConfigure() {
        bind(PhotosLibrarySettings.class).annotatedWith(GooglePhotosClientImpl.Dependency.class)
                .toProvider(PhotosLibrarySettingsProvider.class);
        bind(getExposedKey()).to(boundLifecycleComponent(GooglePhotosClientImpl.class));
        expose(getExposedKey());
    }

    public static final class Builder extends BaseBuilder<ExposedKeyModule<GooglePhotosClient>, Builder> {
        @Override
        public ExposedKeyModule<GooglePhotosClient> build() {
            return new GooglePhotosModule(getSettings());
        }

        @Override
        protected Builder thisBuilder() {
            return this;
        }
    }
}
