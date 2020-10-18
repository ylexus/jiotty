package net.yudichev.jiotty.connector.google.sdm;

import java.util.concurrent.CompletableFuture;

public interface GoogleNestThermostat {
    CompletableFuture<Mode> getCurrentMode();

    CompletableFuture<Void> setMode(Mode mode);

    enum Mode {
        HEAT, ECO, COOL, HEATCOOL, OFF
    }
}
