package net.yudichev.jiotty.connector.tesla.fleet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseTelemetryCreateConfigSkippedVehicles {
    @JsonProperty("missing_key")
    List<String> vehiclesMissingKey();

    @JsonProperty("unsupported_hardware")
    List<String> vehiclesWithUnsupportedHardware();

    @JsonProperty("unsupported_firmware")
    List<String> vehiclesWithUnsupportedFirmware();

    @JsonProperty("max_configs")
    List<String> vehiclesWithMaxConfigsReached();
}
