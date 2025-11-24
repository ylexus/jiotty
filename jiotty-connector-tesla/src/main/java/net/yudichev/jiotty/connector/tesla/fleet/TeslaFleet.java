package net.yudichev.jiotty.connector.tesla.fleet;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TeslaFleet {
    CompletableFuture<List<TeslaVehicleData>> listVehicles();

    TeslaVehicle vehicle(String vehicleVin);
}
