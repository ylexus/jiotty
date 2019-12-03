package net.yudichev.jiotty.connector.google.common.impl;

import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.connector.google.common.AuthorizationBrowser;
import net.yudichev.jiotty.connector.google.common.GoogleApiAuthSettings;
import net.yudichev.jiotty.connector.google.common.ResolvedGoogleApiAuthSettings;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Optional;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

final class GoogleApiAuthSettingsResolver implements Provider<ResolvedGoogleApiAuthSettings> {
    private final ResolvedGoogleApiAuthSettings resolvedSettings;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Inject
    GoogleApiAuthSettingsResolver(@Dependency GoogleApiAuthSettings unresolvedSettings,
                                  @Dependency Optional<AuthorizationBrowser> authorizationBrowser) {
        resolvedSettings = ResolvedGoogleApiAuthSettings.builder()
                .setApplicationName(unresolvedSettings.applicationName())
                .setAuthDataStoreRootDir(unresolvedSettings.authDataStoreRootDir())
                .setCredentialsUrl(unresolvedSettings.credentialsUrl())
                .setAuthorizationBrowser(authorizationBrowser)
                .build();
    }

    @Override
    public ResolvedGoogleApiAuthSettings get() {
        return resolvedSettings;
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }
}
