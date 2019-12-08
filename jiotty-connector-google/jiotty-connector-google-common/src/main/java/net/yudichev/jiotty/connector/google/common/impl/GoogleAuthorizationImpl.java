package net.yudichev.jiotty.connector.google.common.impl;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.auth.Credentials;
import com.google.auth.oauth2.UserCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

final class GoogleAuthorizationImpl implements GoogleAuthorization {
    private final Credential credential;
    private final GoogleClientSecrets clientSecrets;

    @SuppressWarnings("ConstructorWithTooManyParameters")
        // internal API
    GoogleAuthorizationImpl(NetHttpTransport httpTransport,
                            Path authDataStoreRootDir,
                            String apiName,
                            URL credentialsUrl,
                            List<String> scopes,
                            AuthorizationCodeInstalledApp.Browser browser) {
        try {
            // Load client secrets.
            try (InputStream in = credentialsUrl.openStream()) {
                clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new InputStreamReader(in));
            }

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JacksonFactory.getDefaultInstance(), clientSecrets, scopes)
                    .setDataStoreFactory(new FileDataStoreFactory(authDataStoreRootDir.resolve(apiName).toFile()))
                    .setAccessType("offline")
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setPort(8888)
                    .build();
            credential = new AuthorizationCodeInstalledApp(flow, receiver, browser).authorize("user");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Google authorization", e);
        }
    }

    @Override
    public Credential getCredential() {
        return credential;
    }

    @Override
    public Credentials getCredentials() {
        String clientId = clientSecrets.getDetails().getClientId();
        String clientSecret = clientSecrets.getDetails().getClientSecret();

        return UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(credential.getRefreshToken())
                .build();
    }
}
