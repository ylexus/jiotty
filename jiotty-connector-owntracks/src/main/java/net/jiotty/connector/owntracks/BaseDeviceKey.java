package net.jiotty.connector.owntracks;

import net.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@PublicImmutablesStyle
interface BaseDeviceKey {
    @Value.Parameter
    String userName();

    @Value.Parameter
    String deviceName();
}
