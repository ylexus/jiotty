package net.yudichev.jiotty.connector.owntracks;

import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@PublicImmutablesStyle
interface BaseDeviceKey {
    @Value.Parameter
    String userName();

    @Value.Parameter
    String deviceName();
}
