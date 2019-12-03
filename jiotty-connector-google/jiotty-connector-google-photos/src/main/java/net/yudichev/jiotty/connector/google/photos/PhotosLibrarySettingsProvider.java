package net.yudichev.jiotty.connector.google.photos;

import com.google.photos.library.v1.PhotosLibrarySettings;
import net.yudichev.jiotty.connector.google.common.ResolvedGoogleApiAuthSettings;
import net.yudichev.jiotty.connector.google.common.impl.GoogleAuthorization;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;
import static com.google.api.gax.core.FixedCredentialsProvider.create;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.yudichev.jiotty.connector.google.common.impl.Bindings.Settings;

final class PhotosLibrarySettingsProvider implements Provider<PhotosLibrarySettings> {
    private static final String SCOPE_PHOTOS_LIBRARY = "https://www.googleapis.com/auth/photoslibrary";
    private final ResolvedGoogleApiAuthSettings settings;

    @Inject
    PhotosLibrarySettingsProvider(@Settings ResolvedGoogleApiAuthSettings settings) {
        this.settings = checkNotNull(settings);
    }

    @Override
    public PhotosLibrarySettings get() {
        return getAsUnchecked(() -> PhotosLibrarySettings.newBuilder()
                .setCredentialsProvider(create(
                        GoogleAuthorization.builder()
                                .setHttpTransport(newTrustedTransport())
                                .setAuthDataStoreRootDir(settings.authDataStoreRootDir())
                                .setApiName("photos")
                                .setCredentialsUrl(settings.credentialsUrl())
                                .addRequiredScope(SCOPE_PHOTOS_LIBRARY)
                                .withBrowser(settings.authorizationBrowser())
                                .build()
                                .getCredentials()))
                .build());
    }
}
