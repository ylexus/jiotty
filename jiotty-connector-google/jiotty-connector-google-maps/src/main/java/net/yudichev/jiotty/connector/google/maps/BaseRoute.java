package net.yudichev.jiotty.connector.google.maps;

import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value.Immutable;

import java.time.Duration;

@Immutable
@PublicImmutablesStyle
interface BaseRoute {
    int distanceMetres();

    Duration duration();
}
