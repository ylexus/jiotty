package net.yudichev.jiotty.connector.homeassistant;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface HomeAssistantClient {
    Climate climate();

    Switch aSwitch();

    Number number();

    Button button();

    Domain sensor();

    interface Domain {
        CompletableFuture<HAState> getState(String domainlessEntityId);
    }

    interface Climate extends Domain {
        CompletableFuture<List<HAState>> setTemperature(String entityId, String hvacMode, double temperature);

        CompletableFuture<List<HAState>> turnOn(String entityId);

        CompletableFuture<List<HAState>> turnOff(String entityId);
    }

    interface Switch extends Domain {
        CompletableFuture<List<HAState>> turnOn(String entityId);

        CompletableFuture<List<HAState>> turnOff(String entityId);
    }

    interface Number extends Domain {
        CompletableFuture<List<HAState>> setValue(String entityId, double value);
    }

    interface Button extends Domain {
        CompletableFuture<List<HAState>> press(String entityId);
    }
}
