package net.yudichev.jiotty.connector.nest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.CompletableFuture;

public interface NestThermostat {
    CompletableFuture<Mode> currentMode();

    CompletableFuture<Mode> setMode(Mode mode);

    enum Mode {
        @JsonProperty("heat")
        HEAT("heat"),
        @JsonProperty("cool")
        COOL("cool"),
        @JsonProperty("heat-cool")
        HEAT_COOL("heat-cool"),
        @JsonProperty("eco")
        ECO("eco"),
        @JsonProperty("off")
        OFF("off");

        private final String id;

        Mode(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }
}
