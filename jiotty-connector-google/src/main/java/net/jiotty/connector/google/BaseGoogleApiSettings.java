package net.jiotty.connector.google;

import net.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.net.URL;

@Immutable
@PublicImmutablesStyle
interface BaseGoogleApiSettings {
    @Value.Parameter
    String applicationName();

    @Value.Parameter
    URL credentialsUrl();
}
