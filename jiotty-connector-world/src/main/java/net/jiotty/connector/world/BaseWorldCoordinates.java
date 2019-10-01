package net.jiotty.connector.world;

import net.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@PublicImmutablesStyle
interface BaseWorldCoordinates {
    @Value.Parameter
    double getLatitude();

    @Value.Parameter
    double getLongitude();
}
