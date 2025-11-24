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
interface BaseVehicleData {
    String state();

    @JsonProperty("charge_state")
    ChargeState chargeState();

    @JsonProperty("drive_state")
    DriveState driveState();

    @JsonProperty("climate_state")
    ClimateState climateState();
}
