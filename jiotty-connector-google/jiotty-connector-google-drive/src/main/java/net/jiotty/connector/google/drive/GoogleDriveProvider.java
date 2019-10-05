package net.jiotty.connector.google.drive;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.common.collect.ImmutableList;
import net.jiotty.connector.google.common.impl.GoogleApiSettings;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.jiotty.connector.google.common.impl.Bindings.Settings;
import static net.jiotty.connector.google.common.impl.GoogleAuthorization.authorize;

public final class GoogleDriveProvider implements Provider<Drive> {
    private final GoogleApiSettings settings;

    @Inject
    public GoogleDriveProvider(@Settings GoogleApiSettings settings) {
        this.settings = checkNotNull(settings);
    }

    @Override
    public Drive get() {
        return getAsUnchecked(() -> {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            return new Drive.Builder(HTTP_TRANSPORT,
                    JacksonFactory.getDefaultInstance(),
                    authorize(HTTP_TRANSPORT, "gdrive", settings.credentialsUrl(), ImmutableList.of(DriveScopes.DRIVE)).getCredential())
                    .setApplicationName(settings.applicationName())
                    .build();
        });
    }
}
