package net.yudichev.jiotty.connector.tesla.fleet;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface TeslaVehicle {
    CompletableFuture<Void> wakeUp();

    CompletableFuture<Optional<String>> setChargeLimit(int limitPercent);

    CompletableFuture<Optional<String>> startCharge();

    CompletableFuture<Optional<String>> stopCharge();

    CompletableFuture<Optional<String>> startAutoConditioning();

    CompletableFuture<Optional<String>> stopAutoConditioning();

    CompletableFuture<TelemetryGetConfigResponse> telemetryGetConfig();

    CompletableFuture<TelemetryDeleteConfigResponse> telemetryDeleteConfig();

    CompletableFuture<TelemetryFleetStatus> telemetryFleetStatus();

    CompletableFuture<TelemetryFleetErrorsResponse> telemetryFleetErrors();

    /**
     * @return {@link Optional#empty()} if the vehicle is not online
     */
    CompletableFuture<Optional<VehicleData>> getData(Set<Endpoint> endpoints);

    String vin();

    enum Endpoint {
        CHARGE_STATE("charge_state"),
        CLIMATE_STATE("climate_state"),
        CLOSURES_STATE("closures_state"),
        DRIVE_STATE("drive_state"),
        GUI_SETTINGS("gui_settings"),
        LOCATION_DATA("location_data"),
        CHARGE_SCHEDULE_DATA("charge_schedule_data"),
        PRECONDITIONING_SCHEDULE_DATA("preconditioning_schedule_data"),
        VEHICLE_CONFIG("vehicle_config"),
        VEHICLE_STATE("vehicle_state"),
        VEHICLE_DATA_COMBO("vehicle_data_combo"),
        ;

        private final String id;

        Endpoint(String id) {
            this.id = id;
        }

        String id() {
            return id;
        }
    }
}
