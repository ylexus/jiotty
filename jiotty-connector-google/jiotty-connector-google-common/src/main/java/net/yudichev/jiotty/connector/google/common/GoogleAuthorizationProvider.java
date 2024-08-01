package net.yudichev.jiotty.connector.google.common;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.common.collect.ImmutableList;
import com.google.inject.BindingAnnotation;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;

final class GoogleAuthorizationProvider implements Provider<GoogleAuthorization> {

    private final Provider<GoogleApiAuthSettings> settingsProvider;
    private final Provider<URL> credentialsUrlProvider;
    private final Optional<AuthorizationBrowser> authorizationBrowser;
    private final List<String> requiredScopes;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Inject
    GoogleAuthorizationProvider(@Dependency Provider<GoogleApiAuthSettings> settingsProvider,
                                @Dependency Provider<URL> credentialsUrlProvider,
                                @Dependency Optional<AuthorizationBrowser> authorizationBrowser,
                                @Scopes Collection<String> requiredScopes) {
        this.settingsProvider = checkNotNull(settingsProvider);
        this.credentialsUrlProvider = checkNotNull(credentialsUrlProvider);
        this.authorizationBrowser = checkNotNull(authorizationBrowser);
        this.requiredScopes = ImmutableList.copyOf(requiredScopes);
    }

    @Override
    public GoogleAuthorization get() {
        GoogleApiAuthSettings settings = settingsProvider.get();
        URL credentialsUrl = credentialsUrlProvider.get();
        return GoogleAuthorization.builder()
                .setHttpTransport(getAsUnchecked(GoogleNetHttpTransport::newTrustedTransport))
                .setAuthDataStoreRootDir(settings.authDataStoreRootDir())
                .setCredentialsUrl(credentialsUrl)
                .setLocalReceiverHostName(settings.localReceiverHostName())
                .addRequiredScopes(requiredScopes)
                .withBrowser(authorizationBrowser)
                .build();
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {}

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Scopes {}
}
