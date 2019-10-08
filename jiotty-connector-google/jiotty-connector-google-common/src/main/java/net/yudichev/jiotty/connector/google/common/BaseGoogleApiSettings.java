package net.yudichev.jiotty.connector.google.common;

import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
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
