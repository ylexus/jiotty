package net.yudichev.jiotty.connector.homeassistant;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface HomeAssistantClient {
    Climate climate();

    Switch aSwitch();

    Number number();

    interface Climate {
        CompletableFuture<List<HAState>> setTemperature(String entityId, String hvacMode, double temperature);

        CompletableFuture<List<HAState>> turnOn(String entityId);

        CompletableFuture<List<HAState>> turnOff(String entityId);
    }

    interface Switch {
        CompletableFuture<List<HAState>> turnOn(String entityId);

        CompletableFuture<List<HAState>> turnOff(String entityId);
    }

    interface Number {
        CompletableFuture<List<HAState>> setValue(String entityId, double value);
    }
}
