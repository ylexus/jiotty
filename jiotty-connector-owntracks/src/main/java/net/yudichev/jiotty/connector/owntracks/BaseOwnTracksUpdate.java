package net.yudichev.jiotty.connector.owntracks;

import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
@PublicImmutablesStyle
interface BaseOwnTracksUpdate<T> {
    @Value.Parameter
    DeviceKey deviceKey();

    @Value.Parameter
    T payload();
}
