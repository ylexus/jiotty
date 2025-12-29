package net.yudichev.jiotty.connector.tesla.fleet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@Value.Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseTelemetryFleetStatus {
    @JsonProperty("key_paired_vins")
    List<String> keyPairedVins();

    @JsonProperty("unpaired_vins")
    List<String> unpairedVins();

    @JsonProperty("vehicle_info")
    Map<String, TelemetryFleetStatusVehicleInfo> vehicleInfoByVin();
}
