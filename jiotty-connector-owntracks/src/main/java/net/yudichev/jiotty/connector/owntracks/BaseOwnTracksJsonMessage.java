package net.yudichev.jiotty.connector.owntracks;

import net.yudichev.jiotty.common.lang.PackagePrivateImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
@PackagePrivateImmutablesStyle
interface BaseOwnTracksJsonMessage {
    @Value.Parameter
    DeviceKey deviceKey();

    @Value.Parameter
    String payload();
}
