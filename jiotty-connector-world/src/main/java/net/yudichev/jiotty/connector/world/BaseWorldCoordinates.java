package net.yudichev.jiotty.connector.world;

import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@PublicImmutablesStyle
interface BaseWorldCoordinates {
    @Value.Parameter
    double getLatitude();

    @Value.Parameter
    double getLongitude();
}
