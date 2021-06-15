package net.yudichev.jiotty.connector.google.common;

import com.google.api.client.auth.oauth2.Credential;
import com.google.auth.Credentials;

public interface GoogleAuthorization {
    Credential getCredential();

    Credentials getCredentials();

    static GoogleAuthorizationBuilder builder() {
        return new GoogleAuthorizationBuilder();
    }
}
