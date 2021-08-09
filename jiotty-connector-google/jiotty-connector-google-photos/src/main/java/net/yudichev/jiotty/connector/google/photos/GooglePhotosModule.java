package net.yudichev.jiotty.connector.google.photos;

import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.connector.google.common.GoogleAuthorization;
import net.yudichev.jiotty.connector.google.common.impl.BaseGoogleServiceModule;

public final class GooglePhotosModule extends BaseGoogleServiceModule implements ExposedKeyModule<GooglePhotosClient> {
    private GooglePhotosModule(BindingSpec<GoogleAuthorization> googleAuthorizationSpec) {
        super(googleAuthorizationSpec);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void doConfigure() {
        bind(getExposedKey()).to(registerLifecycleComponent(GooglePhotosClientImpl.class));
        expose(getExposedKey());
    }

    public static final class Builder extends BaseBuilder<GooglePhotosClient, Builder> {
        @Override
        public ExposedKeyModule<GooglePhotosClient> build() {
            return new GooglePhotosModule(getAuthorizationSpec());
        }

        @Override
        protected Builder thisBuilder() {
            return this;
        }
    }
}
