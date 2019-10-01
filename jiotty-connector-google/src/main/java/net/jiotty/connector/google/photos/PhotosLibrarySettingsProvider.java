package net.jiotty.connector.google.photos;

import com.google.common.collect.ImmutableList;
import com.google.photos.library.v1.PhotosLibrarySettings;
import net.jiotty.connector.google.GoogleApiSettings;

import javax.inject.Inject;
import javax.inject.Provider;
import java.net.URL;

import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;
import static com.google.api.gax.core.FixedCredentialsProvider.create;
import static net.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.jiotty.connector.google.impl.Bindings.Settings;
import static net.jiotty.connector.google.impl.GoogleAuthorization.authorize;

final class PhotosLibrarySettingsProvider implements Provider<PhotosLibrarySettings> {
    private static final String SCOPE_PHOTOS_LIBRARY = "https://www.googleapis.com/auth/photoslibrary";
    private final URL credentialsUrl;

    @Inject
    PhotosLibrarySettingsProvider(@Settings GoogleApiSettings settings) {
        this.credentialsUrl = settings.credentialsUrl();
    }

    @Override
    public PhotosLibrarySettings get() {
        return getAsUnchecked(() -> PhotosLibrarySettings.newBuilder()
                .setCredentialsProvider(create(
                        authorize(newTrustedTransport(), "photos", credentialsUrl, ImmutableList.of(SCOPE_PHOTOS_LIBRARY)).getCredentials()))
                .build());
    }
}
