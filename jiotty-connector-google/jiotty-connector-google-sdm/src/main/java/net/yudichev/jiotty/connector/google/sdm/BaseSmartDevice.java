package net.yudichev.jiotty.connector.google.sdm;

import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;

import static org.immutables.value.Value.Immutable;
import static org.immutables.value.Value.Parameter;

@Immutable
@PublicImmutablesStyle
interface BaseSmartDevice {
    @Parameter
    String name();

    @Parameter
    String type();
}
