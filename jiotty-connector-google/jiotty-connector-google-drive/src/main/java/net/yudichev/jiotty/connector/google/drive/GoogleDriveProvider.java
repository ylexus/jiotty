package net.yudichev.jiotty.connector.google.drive;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.common.collect.ImmutableSet;
import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.connector.google.common.ResolvedGoogleApiAuthSettings;
import net.yudichev.jiotty.connector.google.common.impl.GoogleAuthorization;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.yudichev.jiotty.connector.google.common.impl.Bindings.Settings;

public final class GoogleDriveProvider implements Provider<Drive> {
    private final ResolvedGoogleApiAuthSettings settings;
    private final Set<String> scopes;

    @Inject
    public GoogleDriveProvider(@Settings ResolvedGoogleApiAuthSettings settings,
                               @Scopes Set<String> scopes) {
        this.settings = checkNotNull(settings);
        checkArgument(!scopes.isEmpty(), "at least one scope is required");
        this.scopes = ImmutableSet.copyOf(scopes);
    }

    @Override
    public Drive get() {
        return getAsUnchecked(() -> {
            // Build a new authorized API client service.
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            var builder = GoogleAuthorization.builder()
                    .setHttpTransport(httpTransport)
                    .setAuthDataStoreRootDir(settings.authDataStoreRootDir())
                    .setApiName("gdrive")
                    .setCredentialsUrl(settings.credentialsUrl())
                    .withBrowser(settings.authorizationBrowser());
            scopes.forEach(builder::addRequiredScope);
            return new Drive.Builder(httpTransport,
                    JacksonFactory.getDefaultInstance(),
                    builder.build()
                            .getCredential())
                    .setApplicationName(settings.applicationName())
                    .build();
        });
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Scopes {
    }

}
