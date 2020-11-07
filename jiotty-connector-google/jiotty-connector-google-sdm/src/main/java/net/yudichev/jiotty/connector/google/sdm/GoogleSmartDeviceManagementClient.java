package net.yudichev.jiotty.connector.google.sdm;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public interface GoogleSmartDeviceManagementClient {
    GoogleNestThermostat getThermostat(String deviceId, Executor executor);

    default GoogleNestThermostat getThermostat(String deviceId) {
        return getThermostat(deviceId, ForkJoinPool.commonPool());
    }
}