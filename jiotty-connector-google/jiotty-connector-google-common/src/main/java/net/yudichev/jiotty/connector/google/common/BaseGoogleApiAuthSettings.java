package net.yudichev.jiotty.connector.google.common;

import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

@Immutable
@PublicImmutablesStyle
interface BaseGoogleApiAuthSettings {
    Path authDataStoreRootDir();

    @Value.Default
    default String localReceiverHostName() {
        return "localhost";
    }

    String applicationName();

    BindingSpec<URL> credentialsUrl();

    Optional<BindingSpec<AuthorizationBrowser>> authorizationBrowser();
}
