package net.yudichev.jiotty.connector.google.sheets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import net.yudichev.jiotty.connector.google.common.GoogleApiSettings;
import net.yudichev.jiotty.connector.google.common.impl.GoogleAuthorization;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.google.api.services.sheets.v4.SheetsScopes.SPREADSHEETS;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.yudichev.jiotty.connector.google.common.impl.Bindings.Settings;

final class CredentialProvider implements Provider<Credential> {
    private final NetHttpTransport netHttpTransport;
    private final GoogleApiSettings settings;

    @Inject
    CredentialProvider(NetHttpTransport netHttpTransport,
                       @Settings GoogleApiSettings settings) {
        this.netHttpTransport = checkNotNull(netHttpTransport);
        this.settings = checkNotNull(settings);
    }

    @Override
    public Credential get() {
        return getAsUnchecked(() ->
                GoogleAuthorization.builder()
                        .setHttpTransport(netHttpTransport)
                        .setAuthDataStoreRootDir(settings.authDataStoreRootDir())
                        .setApiName("gsheets")
                        .setCredentialsUrl(settings.credentialsUrl())
                        .addRequiredScope(SPREADSHEETS)
                        .withBrowser(settings.authorizationBrowser())
                        .build()
                        .getCredential());
    }
}
