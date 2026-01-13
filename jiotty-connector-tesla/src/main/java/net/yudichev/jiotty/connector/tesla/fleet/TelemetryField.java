package net.yudichev.jiotty.connector.tesla.fleet;

import com.google.common.collect.ImmutableSet;
import net.yudichev.jiotty.common.geo.LatLon;
import net.yudichev.jiotty.common.lang.Json;
import net.yudichev.jiotty.common.lang.MoreThrowables;

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public sealed interface TelemetryField permits
        TelemetryField.TDetailedChargeState,
        TelemetryField.TBatteryLevel,
        TelemetryField.TChargeLimitSoc,
        TelemetryField.TLocation,
        TelemetryField.THvacPower,
        TelemetryField.TInsideTemp,
        TelemetryField.THvacLeftTemperatureRequest,
        TelemetryField.THvacRightTemperatureRequest {

    @SuppressWarnings("PublicStaticCollectionField") // immutable
    ImmutableSet<String> ALL_NAMES = Stream.of(TelemetryField.class.getPermittedSubclasses())
                                           .map(clazz -> MoreThrowables.getAsUnchecked(() -> (String) clazz.getDeclaredField("NAME").get(null)))
                                           .collect(toImmutableSet());

    String fieldName();

    enum THvacPower implements TelemetryField {
        UNKNOWN,
        OFF,
        ON,
        PRECONDITION,
        OVERHEAT_PROTECT;

        public static final String NAME = "HvacPower";

        public static THvacPower decode(String jsonValue) {
            return switch (Json.parse(jsonValue, String.class)) {
                case "HvacPowerStateOff" -> OFF;
                case "HvacPowerStateOn" -> ON;
                case "HvacPowerStatePrecondition" -> PRECONDITION;
                case "HvacPowerStateOverheatProtect" -> OVERHEAT_PROTECT;
                default -> UNKNOWN;
            };
        }

        @Override
        public String fieldName() {
            return NAME;
        }

        @Override
        public String toString() {
            // To look consistent with the other fields
            return "THvacPower[" + name() + ']';
        }
    }

    record TDetailedChargeState(TeslaChargingState state) implements TelemetryField {
        public static final TDetailedChargeState DISCONNECTED = new TDetailedChargeState(TeslaChargingState.DISCONNECTED);
        public static final TDetailedChargeState NO_POWER = new TDetailedChargeState(TeslaChargingState.NO_POWER);
        public static final TDetailedChargeState STARTING = new TDetailedChargeState(TeslaChargingState.STARTING);
        public static final TDetailedChargeState CHARGING = new TDetailedChargeState(TeslaChargingState.CHARGING);
        public static final TDetailedChargeState COMPLETE = new TDetailedChargeState(TeslaChargingState.COMPLETE);
        public static final TDetailedChargeState STOPPED = new TDetailedChargeState(TeslaChargingState.STOPPED);
        public static final TDetailedChargeState UNKNOWN = new TDetailedChargeState(TeslaChargingState.UNKNOWN);
        public static final String NAME = "DetailedChargeState";

        public static TDetailedChargeState decode(String jsonValue) {
            return switch (Json.parse(jsonValue, String.class)) {
                case "DetailedChargeStateDisconnected" -> DISCONNECTED;
                case "DetailedChargeStateNoPower" -> NO_POWER;
                case "DetailedChargeStateStarting" -> STARTING;
                case "DetailedChargeStateCharging" -> CHARGING;
                case "DetailedChargeStateComplete" -> COMPLETE;
                case "DetailedChargeStateStopped" -> STOPPED;
                default -> UNKNOWN;
            };
        }

        @Override
        public String fieldName() {
            return NAME;
        }
    }

    record TBatteryLevel(double value) implements TelemetryField {
        public static final String NAME = "BatteryLevel";

        @Override
        public String fieldName() {
            return NAME;
        }
    }

    record TChargeLimitSoc(int soc) implements TelemetryField {
        public static final String NAME = "ChargeLimitSoc";

        @Override
        public String fieldName() {
            return NAME;
        }
    }

    record TLocation(LatLon latLon) implements TelemetryField {
        public static final String NAME = "Location";

        public static TLocation decode(TelemetryLocation jsonValue) {
            return new TLocation(new LatLon(jsonValue.latitude(), jsonValue.longitude()));
        }

        @Override
        public String fieldName() {
            return NAME;
        }
    }

    record TInsideTemp(double value) implements TelemetryField {
        public static final String NAME = "InsideTemp";

        @Override
        public String fieldName() {
            return NAME;
        }
    }

    record THvacLeftTemperatureRequest(double value) implements TelemetryField {
        public static final String NAME = "HvacLeftTemperatureRequest";

        @Override
        public String fieldName() {
            return NAME;
        }
    }

    record THvacRightTemperatureRequest(double value) implements TelemetryField {
        public static final String NAME = "HvacRightTemperatureRequest";

        @Override
        public String fieldName() {
            return NAME;
        }
    }
}
