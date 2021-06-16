package net.yudichev.jiotty.connector.google.sheets;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import net.yudichev.jiotty.connector.google.common.GoogleAuthorization;
import net.yudichev.jiotty.connector.google.common.impl.Bindings.Authorization;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.google.common.base.Preconditions.checkNotNull;

final class SheetsProvider implements Provider<Sheets> {
    private final Provider<GoogleAuthorization> googleAuthorizationProvider;

    @Inject
    SheetsProvider(@Authorization Provider<GoogleAuthorization> googleAuthorizationProvider) {
        this.googleAuthorizationProvider = checkNotNull(googleAuthorizationProvider);
    }

    @Override
    public Sheets get() {
        var credential = googleAuthorizationProvider.get().getCredential();
        return new Sheets.Builder(credential.getTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("jiotty")
                .build();
    }
}
