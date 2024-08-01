package net.yudichev.jiotty.connector.google.common;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.collect.ImmutableList;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class GoogleAuthorizationBuilder implements TypedBuilder<GoogleAuthorization> {
    private final ImmutableList.Builder<String> requiredScopesBuilder = ImmutableList.builder();
    private NetHttpTransport httpTransport;
    private Path authDataStoreRootDir;
    private URL credentialsUrl;
    private AuthorizationCodeInstalledApp.Browser browser = new AuthorizationCodeInstalledApp.DefaultBrowser();
    private String localReceiverHostName = "localhost";

    public GoogleAuthorizationBuilder setHttpTransport(NetHttpTransport httpTransport) {
        this.httpTransport = checkNotNull(httpTransport);
        return this;
    }

    public GoogleAuthorizationBuilder setAuthDataStoreRootDir(Path authDataStoreRootDir) {
        this.authDataStoreRootDir = checkNotNull(authDataStoreRootDir);
        return this;
    }

    public GoogleAuthorizationBuilder setLocalReceiverHostName(String localReceiverHostName) {
        this.localReceiverHostName = checkNotNull(localReceiverHostName);
        return this;
    }

    public GoogleAuthorizationBuilder setCredentialsUrl(URL credentialsUrl) {
        this.credentialsUrl = checkNotNull(credentialsUrl);
        return this;
    }

    public GoogleAuthorizationBuilder addRequiredScope(String requiredScope) {
        requiredScopesBuilder.add(requiredScope);
        return this;
    }

    public GoogleAuthorizationBuilder addRequiredScopes(Iterable<String> requiredScopes) {
        requiredScopesBuilder.addAll(requiredScopes);
        return this;
    }

    public GoogleAuthorizationBuilder withBrowser(AuthorizationBrowser browser) {
        return withBrowser(Optional.of(browser));
    }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "BoundedWildcard"})
    public GoogleAuthorizationBuilder withBrowser(Optional<AuthorizationBrowser> browser) {
        this.browser = browser
                .<AuthorizationCodeInstalledApp.Browser>map(authorizationBrowser -> authorizationBrowser::browse)
                .orElseGet(AuthorizationCodeInstalledApp.DefaultBrowser::new);
        return this;
    }

    @Override
    public GoogleAuthorization build() {
        return new GoogleAuthorizationImpl(httpTransport, authDataStoreRootDir, localReceiverHostName, credentialsUrl, requiredScopesBuilder.build(), browser);
    }
}
