package net.yudichev.jiotty.connector.tesla.fleet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;

@Value.Immutable
@PublicImmutablesStyle
@JsonSerialize
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseTelemetryConfig {
    @JsonProperty("ca")
    String caCertificate();

    String hostname();

    int port();

    @JsonProperty("fields")
    Map<String, TelemetryFieldParams> fieldParams();

    @JsonProperty("delivery_policy")
    Optional<String> deliveryPolicy();
}
