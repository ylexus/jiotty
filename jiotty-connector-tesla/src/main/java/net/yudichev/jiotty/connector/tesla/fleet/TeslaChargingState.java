package net.yudichev.jiotty.connector.tesla.fleet;

import com.fasterxml.jackson.annotation.JsonProperty;

/// @implNote Values sneaked from [home assistant integration](https://github.com/home-assistant/core/blob/dev/homeassistant/components/tesla_fleet/sensor.py)
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
