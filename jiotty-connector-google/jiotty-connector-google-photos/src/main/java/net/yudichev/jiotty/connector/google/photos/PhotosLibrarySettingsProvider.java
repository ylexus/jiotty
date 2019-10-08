package net.yudichev.jiotty.connector.google.photos;

import com.google.common.collect.ImmutableList;
import com.google.photos.library.v1.PhotosLibrarySettings;
import net.yudichev.jiotty.connector.google.common.GoogleApiSettings;
import net.yudichev.jiotty.connector.google.common.impl.Bindings;
import net.yudichev.jiotty.connector.google.common.impl.GoogleAuthorization;

import javax.inject.Inject;
import javax.inject.Provider;
import java.net.URL;

import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;
import static com.google.api.gax.core.FixedCredentialsProvider.create;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;

final class PhotosLibrarySettingsProvider implements Provider<PhotosLibrarySettings> {
    private static final String SCOPE_PHOTOS_LIBRARY = "https://www.googleapis.com/auth/photoslibrary";
    private final URL credentialsUrl;

    @Inject
    PhotosLibrarySettingsProvider(@Bindings.Settings GoogleApiSettings settings) {
        credentialsUrl = settings.credentialsUrl();
    }

    @Override
    public PhotosLibrarySettings get() {
        return getAsUnchecked(() -> PhotosLibrarySettings.newBuilder()
                .setCredentialsProvider(create(
                        GoogleAuthorization.authorize(newTrustedTransport(), "photos", credentialsUrl, ImmutableList.of(SCOPE_PHOTOS_LIBRARY)).getCredentials()))
                .build());
    }
}
