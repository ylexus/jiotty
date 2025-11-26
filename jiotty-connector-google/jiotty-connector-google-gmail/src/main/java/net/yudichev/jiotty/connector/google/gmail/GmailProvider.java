package net.yudichev.jiotty.connector.google.gmail;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import net.yudichev.jiotty.connector.google.common.GoogleAuthorization;
import net.yudichev.jiotty.connector.google.common.impl.Bindings.Authorization;

import static com.google.common.base.Preconditions.checkNotNull;

final class GmailProvider implements Provider<Gmail> {
    private final Provider<GoogleAuthorization> googleAuthorizationProvider;

    @Inject
    GmailProvider(@Authorization Provider<GoogleAuthorization> googleAuthorizationProvider) {
        this.googleAuthorizationProvider = checkNotNull(googleAuthorizationProvider);
    }

    @Override
    public Gmail get() {
        return createService(googleAuthorizationProvider.get());
    }

    static Gmail createService(GoogleAuthorization googleAuthorization) {
        var credential = googleAuthorization.getCredential();
        return new Gmail.Builder(credential.getTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("jiotty")
                .build();
    }
}
