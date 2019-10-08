package net.yudichev.jiotty.connector.google.sheets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import net.yudichev.jiotty.connector.google.common.GoogleApiSettings;
import net.yudichev.jiotty.connector.google.common.impl.Bindings;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;

final class SheetsProvider implements Provider<Sheets> {
    private final NetHttpTransport httpTransport;
    private final Credential credential;
    private final String applicationName;

    @Inject
    SheetsProvider(NetHttpTransport httpTransport,
                   Credential credential,
                   @Bindings.Settings GoogleApiSettings settings) {
        this.httpTransport = checkNotNull(httpTransport);
        this.credential = checkNotNull(credential);
        applicationName = settings.applicationName();
    }

    @Override
    public Sheets get() {
        return getAsUnchecked(() -> new Sheets.Builder(httpTransport, JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(applicationName)
                .build());
    }
}
