package net.jiotty.connector.owntracks;

import net.jiotty.common.lang.PublicImmutablesStyle;
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
