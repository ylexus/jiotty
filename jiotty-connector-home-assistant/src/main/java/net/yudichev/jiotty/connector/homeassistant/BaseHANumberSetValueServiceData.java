package net.yudichev.jiotty.connector.homeassistant;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.yudichev.jiotty.common.lang.PackagePrivateImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@PackagePrivateImmutablesStyle
@JsonSerialize
public interface BaseHANumberSetValueServiceData extends BaseHAServiceData {
    @Value.Parameter
    double value();
}
