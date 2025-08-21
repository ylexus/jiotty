package net.yudichev.jiotty.connector.google.maps;

import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value.Immutable;

import java.util.List;

@Immutable
@PublicImmutablesStyle
interface BaseRoutes {
    List<Route> routes();
}
