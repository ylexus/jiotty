package net.yudichev.jiotty.connector.tesla.fleet;

import com.google.common.reflect.TypeToken;
import net.yudichev.jiotty.common.lang.Json;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TelemetryFleetStatusTest {
    @Test
    void parses() {
        var responseJson = """
                           {
                             "response": {
                               "key_paired_vins": [
                                 "VIN"
                               ],
                               "unpaired_vins": [],
                               "vehicle_info": {
                                 "VIN": {
                                   "firmware_version": "2025.44.25",
                                   "vehicle_command_protocol_required": true,
                                   "discounted_device_data": false,
                                   "fleet_telemetry_version": "1.1.0",
                                   "total_number_of_keys": 8
                                 }
                               }
                             }
                           }
                           """;
        assertThat(Json.parse(responseJson, new TypeToken<ResponseWrapper<TelemetryFleetStatus>>() {})).isEqualTo(
                ResponseWrapper.<TelemetryFleetStatus>builder()
                               .setResponse(TelemetryFleetStatus.builder()
                                                                .setKeyPairedVins(List.of("VIN"))
                                                                .setUnpairedVins(List.of())
                                                                .setVehicleInfoByVin(Map.of(
                                                                        "VIN", TelemetryFleetStatusVehicleInfo.builder()
                                                                                                              .setFirmwareVersion("2025.44.25")
                                                                                                              .setVehicleCommandProtocolRequired(true)
                                                                                                              .setDiscountedDeviceData(false)
                                                                                                              .setFleetTelemetryVersion("1.1.0")
                                                                                                              .setTotalNumberOfKeys(8)
                                                                                                              .build()))
                                                                .build())
                               .build());
    }
}