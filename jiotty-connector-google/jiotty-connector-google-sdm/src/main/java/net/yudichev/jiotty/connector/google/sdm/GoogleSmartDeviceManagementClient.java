package net.yudichev.jiotty.connector.google.sdm;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public interface GoogleSmartDeviceManagementClient {
    CompletableFuture<List<SmartDevice>> listDevices(Executor executor);

    default CompletableFuture<List<SmartDevice>> listDevices() {
        return listDevices(ForkJoinPool.commonPool());
    }

    GoogleNestThermostat getThermostat(String deviceId, Executor executor);

    default GoogleNestThermostat getThermostat(String deviceId) {
        return getThermostat(deviceId, ForkJoinPool.commonPool());
    }
}