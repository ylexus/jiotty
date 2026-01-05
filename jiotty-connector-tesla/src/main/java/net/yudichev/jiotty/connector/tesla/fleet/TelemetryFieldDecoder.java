package net.yudichev.jiotty.connector.tesla.fleet;

import jakarta.annotation.Nullable;
import net.yudichev.jiotty.common.lang.Json;

final class TelemetryFieldDecoder {
    /// @return `null` if the `fieldName` is unsupported
    public static @Nullable TelemetryField decode(String fieldName, String jsonData) {
        return switch (fieldName) {
            case "DetailedChargeState" -> TelemetryField.TDetailedChargeState.decode(jsonData);
            case "BatteryLevel" -> new TelemetryField.TBatteryLevel(decodeDouble(jsonData));
            case "ChargeLimitSoc" -> new TelemetryField.TChargeLimitSoc(decodeInt(jsonData));
            case "Location" -> TelemetryField.TLocation.decode(Json.parse(jsonData, TelemetryLocation.class));
            case "PreconditioningEnabled" -> new TelemetryField.TPreconditioningEnabled(decodeBoolean(jsonData));
            case "InsideTemp" -> new TelemetryField.TInsideTemp(decodeDouble(jsonData));
            case "HvacLeftTemperatureRequest" -> new TelemetryField.THvacLeftTemperatureRequest(decodeDouble(jsonData));
            case "HvacRightTemperatureRequest" -> new TelemetryField.THvacRightTemperatureRequest(decodeDouble(jsonData));
            default -> null;
        };
    }

    private static boolean decodeBoolean(String jsonData) {
        return Boolean.parseBoolean(jsonData);
    }

    private static int decodeInt(String jsonData) {
        return Integer.parseInt(jsonData);
    }

    private static double decodeDouble(String jsonData) {
        return Double.parseDouble(jsonData);
    }
}
