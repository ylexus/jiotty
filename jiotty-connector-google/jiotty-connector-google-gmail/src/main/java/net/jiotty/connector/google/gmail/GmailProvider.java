package net.jiotty.connector.google.gmail;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.common.collect.ImmutableList;
import net.jiotty.connector.google.common.impl.GoogleApiSettings;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.api.services.gmail.GmailScopes.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.jiotty.connector.google.common.impl.Bindings.Settings;
import static net.jiotty.connector.google.common.impl.GoogleAuthorization.authorize;

final class GmailProvider implements Provider<Gmail> {
    private static final List<String> SCOPES = ImmutableList.of(GMAIL_SEND, GMAIL_MODIFY, GMAIL_READONLY, GMAIL_LABELS);
    private static final Map<GoogleApiSettings, Gmail> serviceBySettings = new ConcurrentHashMap<>();
    private final GoogleApiSettings settings;

    @Inject
    GmailProvider(@Settings GoogleApiSettings settings) {
        this.settings = checkNotNull(settings);
    }

    @Override
    public Gmail get() {
        return getService(settings);
    }

    static Gmail getService(GoogleApiSettings settings) {
        return serviceBySettings.computeIfAbsent(settings, googleApiSettings -> getAsUnchecked(() -> {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            return new Gmail.Builder(HTTP_TRANSPORT,
                    JacksonFactory.getDefaultInstance(),
                    authorize(HTTP_TRANSPORT, "gmail", googleApiSettings.credentialsUrl(), SCOPES).getCredential())
                    .setApplicationName(googleApiSettings.applicationName())
                    .build();
        }));
    }
}
