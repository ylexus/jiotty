package net.jiotty.connector.google.common.impl;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.Credentials;

import java.net.URL;
import java.util.List;

public interface GoogleAuthorization {
    Credential getCredential();

    Credentials getCredentials();

    static GoogleAuthorization authorize(NetHttpTransport httpTransport,
                                         String apiName,
                                         URL credentialsUrl,
                                         List<String> requiredScopes) {
        return new GoogleAuthorizationImpl(httpTransport, apiName, credentialsUrl, requiredScopes);
    }
}
