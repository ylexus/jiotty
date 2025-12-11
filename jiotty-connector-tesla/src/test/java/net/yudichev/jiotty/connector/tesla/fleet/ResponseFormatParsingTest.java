package net.yudichev.jiotty.connector.tesla.fleet;

import com.google.common.reflect.TypeToken;
import net.yudichev.jiotty.common.lang.Json;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Instant;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

final class ResponseFormatParsingTest {
    @ParameterizedTest
    @MethodSource
    void vehicleData(String jsonPath, VehicleData expectedVehicleData) throws IOException {
        try (var resourceAsStream = getClass().getResourceAsStream(jsonPath)) {
            assert resourceAsStream != null;
            ResponseWrapper<VehicleData> responseWrapper = Json.parse(new String(resourceAsStream.readAllBytes(), UTF_8), new TypeToken<>() {});
            assertThat(responseWrapper.response()).hasValue(expectedVehicleData);
        }
    }

    public static Stream<Arguments> vehicleData() {
        return Stream.of(Arguments.of(
                                 "/vehicle_data_1.json",
                                 VehicleData.builder()
                                            .setState("online")
                                            .setChargeState(ChargeState.builder()
                                                                       .setChargingState(TeslaChargingState.DISCONNECTED)
                                                                       .setChargeCableConnected(false)
                                                                       .setBatteryLevel(67)
                                                                       .setChargeLimitSoC(50)
                                                                       .build())
                                            .setDriveState(DriveState.builder()
                                                                     .setLatitude(1.1)
                                                                     .setLongitude(2.2)
                                                                     .setTimestamp(Instant.parse("2025-11-09T01:13:49.379Z"))
                                                                     .build())
                                            .setClimateState(ClimateState.builder()
                                                                         .setClimateOn(false)
                                                                         .setDriverTempSetting(19.5)
                                                                         .setInsideTemp(9.8)
                                                                         .setTimestamp(Instant.parse("2025-11-09T01:13:49.379Z"))
                                                                         .build())
                                            .build()),
                         Arguments.of(
                                 "/vehicle_data_2.json",
                                 VehicleData.builder()
                                            .setState("online")
                                            .setChargeState(ChargeState.builder()
                                                                       .setChargingState(TeslaChargingState.UNKNOWN)
                                                                       .setChargeCableConnected(false)
                                                                       .setBatteryLevel(67)
                                                                       .setChargeLimitSoC(71)
                                                                       .build())
                                            .setDriveState(DriveState.builder()
                                                                     .setLatitude(1.1)
                                                                     .setLongitude(2.2)
                                                                     .setTimestamp(Instant.parse("2025-12-11T01:00:43.940Z"))
                                                                     .build())
                                            .setClimateState(ClimateState.builder()
                                                                         .setClimateOn(false)
                                                                         .setDriverTempSetting(20.5)
                                                                         .setInsideTemp(0.0)
                                                                         .setTimestamp(Instant.parse("2025-12-11T01:00:43.939Z"))
                                                                         .build())
                                            .build()));
    }
}
