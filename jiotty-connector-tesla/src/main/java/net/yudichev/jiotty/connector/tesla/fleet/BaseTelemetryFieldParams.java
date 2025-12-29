package net.yudichev.jiotty.connector.tesla.fleet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonSerialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseTelemetryFieldParams {
    @Value.Parameter
    @JsonProperty("interval_seconds")
    int intervalSeconds();

    @JsonProperty("resend_interval_seconds")
    Optional<Integer> resendIntervalSeconds();

    @JsonProperty("minimum_delta")
    Optional<Double> minimumDelta();
}
