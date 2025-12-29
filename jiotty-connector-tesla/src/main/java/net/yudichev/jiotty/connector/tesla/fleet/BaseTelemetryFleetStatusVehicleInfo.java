package net.yudichev.jiotty.connector.tesla.fleet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseTelemetryFleetStatusVehicleInfo {
    @JsonProperty("firmware_version")
    String firmwareVersion();

    @JsonProperty("vehicle_command_protocol_required")
    boolean vehicleCommandProtocolRequired();

    @JsonProperty("discounted_device_data")
    boolean discountedDeviceData();

    @JsonProperty("fleet_telemetry_version")
    String fleetTelemetryVersion();

    @JsonProperty("total_number_of_keys")
    int totalNumberOfKeys();
}
