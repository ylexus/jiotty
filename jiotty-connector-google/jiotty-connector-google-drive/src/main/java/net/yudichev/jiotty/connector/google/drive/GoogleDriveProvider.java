package net.yudichev.jiotty.connector.google.drive;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import net.yudichev.jiotty.connector.google.common.ResolvedGoogleApiAuthSettings;
import net.yudichev.jiotty.connector.google.common.impl.GoogleAuthorization;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.yudichev.jiotty.connector.google.common.impl.Bindings.Settings;

public final class GoogleDriveProvider implements Provider<Drive> {
    private final ResolvedGoogleApiAuthSettings settings;

    @Inject
    public GoogleDriveProvider(@Settings ResolvedGoogleApiAuthSettings settings) {
        this.settings = checkNotNull(settings);
    }

    @Override
    public Drive get() {
        return getAsUnchecked(() -> {
            // Build a new authorized API client service.
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            return new Drive.Builder(httpTransport,
                    JacksonFactory.getDefaultInstance(),
                    GoogleAuthorization.builder()
                            .setHttpTransport(httpTransport)
                            .setAuthDataStoreRootDir(settings.authDataStoreRootDir())
                            .setApiName("gdrive")
                            .setCredentialsUrl(settings.credentialsUrl())
                            .addRequiredScope(DriveScopes.DRIVE)
                            .withBrowser(settings.authorizationBrowser())
                            .build()
                            .getCredential())
                    .setApplicationName(settings.applicationName())
                    .build();
        });
    }
}
