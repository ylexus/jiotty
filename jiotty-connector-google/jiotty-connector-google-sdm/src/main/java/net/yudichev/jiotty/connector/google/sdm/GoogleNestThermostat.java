package net.yudichev.jiotty.connector.google.sdm;

import java.util.concurrent.CompletableFuture;

public interface GoogleNestThermostat {
    CompletableFuture<Mode> getCurrentMode();

    default CompletableFuture<Void> setModeAndVerify(Mode mode) {
        return setMode(mode, true);
    }

    default CompletableFuture<Void> setMode(Mode mode) {
        return setMode(mode, false);
    }

    CompletableFuture<Void> setMode(Mode mode, boolean verify);

    enum Mode {
        HEAT, ECO, COOL, HEATCOOL, OFF
    }
}
