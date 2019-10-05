package net.jiotty.connector.google.sheets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import net.jiotty.connector.google.common.impl.GoogleApiSettings;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.jiotty.connector.google.common.impl.Bindings.Settings;

final class SheetsProvider implements Provider<Sheets> {
    private final NetHttpTransport httpTransport;
    private final Credential credential;
    private final String applicationName;

    @Inject
    SheetsProvider(NetHttpTransport httpTransport,
                   Credential credential,
                   @Settings GoogleApiSettings settings) {
        this.httpTransport = checkNotNull(httpTransport);
        this.credential = checkNotNull(credential);
        this.applicationName = settings.applicationName();
    }

    @Override
    public Sheets get() {
        return getAsUnchecked(() -> new Sheets.Builder(httpTransport, JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(applicationName)
                .build());
    }
}
