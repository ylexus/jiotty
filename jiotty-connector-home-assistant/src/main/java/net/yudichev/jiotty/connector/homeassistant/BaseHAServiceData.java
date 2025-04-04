package net.yudichev.jiotty.connector.homeassistant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.yudichev.jiotty.common.lang.PackagePrivateImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@PackagePrivateImmutablesStyle
@JsonSerialize
public interface BaseHAServiceData {
    @Value.Parameter
    @JsonProperty("entity_id")
    String entityId();
}
