package net.yudichev.jiotty.connector.google.gmail;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.common.collect.ImmutableList;
import net.yudichev.jiotty.connector.google.common.ResolvedGoogleApiAuthSettings;
import net.yudichev.jiotty.connector.google.common.impl.GoogleAuthorization;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.api.services.gmail.GmailScopes.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.yudichev.jiotty.connector.google.common.impl.Bindings.Settings;

final class GmailProvider implements Provider<Gmail> {
    private static final List<String> SCOPES = ImmutableList.of(GMAIL_SEND, GMAIL_MODIFY, GMAIL_READONLY, GMAIL_LABELS);
    private static final Map<ResolvedGoogleApiAuthSettings, Gmail> serviceBySettings = new ConcurrentHashMap<>();
    private final ResolvedGoogleApiAuthSettings settings;

    @Inject
    GmailProvider(@Settings ResolvedGoogleApiAuthSettings settings) {
        this.settings = checkNotNull(settings);
    }

    @Override
    public Gmail get() {
        return getService(settings);
    }

    static Gmail getService(ResolvedGoogleApiAuthSettings settings) {
        return serviceBySettings.computeIfAbsent(settings, googleApiSettings -> getAsUnchecked(() -> {
            // Build a new authorized API client service.
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            return new Gmail.Builder(httpTransport,
                    JacksonFactory.getDefaultInstance(),
                    GoogleAuthorization.builder()
                            .setHttpTransport(httpTransport)
                            .setAuthDataStoreRootDir(settings.authDataStoreRootDir())
                            .setApiName("gmail")
                            .setCredentialsUrl(settings.credentialsUrl())
                            .addRequiredScopes(SCOPES)
                            .withBrowser(settings.authorizationBrowser())
                            .build()
                            .getCredential())
                    .setApplicationName(googleApiSettings.applicationName())
                    .build();
        }));
    }
}
