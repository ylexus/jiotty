package net.yudichev.jiotty.connector.google.drive;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import net.yudichev.jiotty.connector.google.common.GoogleAuthorization;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.connector.google.common.impl.Bindings.Authorization;

public final class GoogleDriveProvider implements Provider<Drive> {
    private final Provider<GoogleAuthorization> googleAuthorizationProvider;

    @Inject
    public GoogleDriveProvider(@Authorization Provider<GoogleAuthorization> googleAuthorizationProvider) {
        this.googleAuthorizationProvider = checkNotNull(googleAuthorizationProvider);
    }

    @Override
    public Drive get() {
        var credential = googleAuthorizationProvider.get().getCredential();
        return new Drive.Builder(credential.getTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("jiotty")
                .build();
    }
}
