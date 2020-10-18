package net.yudichev.jiotty.connector.google.sdm;

public interface GoogleSmartDeviceManagementClient {
    GoogleNestThermostat getThermostat(String deviceId);
}