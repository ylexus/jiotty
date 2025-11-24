package net.yudichev.jiotty.connector.tesla.fleet;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TeslaChargingState {
    @JsonProperty("Starting")
    STARTING,
    @JsonProperty("Charging")
    CHARGING,
    @JsonProperty("Stopped")
    STOPPED,
    @JsonProperty("Complete")
    COMPLETE,
    @JsonProperty("Disconnected")
    DISCONNECTED,
    @JsonProperty("NoPower")
    NO_POWER,
    @JsonProperty("Unknown")
    UNKNOWN
}
