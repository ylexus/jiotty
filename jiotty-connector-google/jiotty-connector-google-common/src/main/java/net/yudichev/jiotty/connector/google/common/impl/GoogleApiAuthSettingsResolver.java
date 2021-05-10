package net.yudichev.jiotty.connector.google.common.impl;

import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.connector.google.common.AuthorizationBrowser;
import net.yudichev.jiotty.connector.google.common.GoogleApiAuthSettings;
import net.yudichev.jiotty.connector.google.common.ResolvedGoogleApiAuthSettings;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URL;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

final class GoogleApiAuthSettingsResolver implements Provider<ResolvedGoogleApiAuthSettings> {

    private final Provider<GoogleApiAuthSettings> unresolvedSettingsProvider;
    private final Provider<URL> credentialsUrlProvider;
    private final Optional<AuthorizationBrowser> authorizationBrowser;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Inject
    GoogleApiAuthSettingsResolver(@Dependency Provider<GoogleApiAuthSettings> unresolvedSettingsProvider,
                                  @Dependency Provider<URL> credentialsUrlProvider,
                                  @Dependency Optional<AuthorizationBrowser> authorizationBrowser) {
        this.unresolvedSettingsProvider = checkNotNull(unresolvedSettingsProvider);
        this.credentialsUrlProvider = checkNotNull(credentialsUrlProvider);
        this.authorizationBrowser = checkNotNull(authorizationBrowser);
    }

    @Override
    public ResolvedGoogleApiAuthSettings get() {
        GoogleApiAuthSettings unresolvedSettings = unresolvedSettingsProvider.get();
        URL credentialsUrl = credentialsUrlProvider.get();
        return ResolvedGoogleApiAuthSettings.builder()
                .setApplicationName(unresolvedSettings.applicationName())
                .setAuthDataStoreRootDir(unresolvedSettings.authDataStoreRootDir())
                .setCredentialsUrl(credentialsUrl)
                .setAuthorizationBrowser(authorizationBrowser)
                .build();
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }
}
