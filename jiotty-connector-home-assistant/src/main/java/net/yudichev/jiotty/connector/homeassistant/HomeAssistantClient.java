package net.yudichev.jiotty.connector.homeassistant;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface HomeAssistantClient {
    Climate climate();

    Switch aSwitch();

    Number number();

    Button button();

    Domain sensor();

    LogBook logBook();

    History history();

    BinarySensor binarySensor();

    interface Domain {
        CompletableFuture<HAState> getState(String domainlessEntityId);
    }

    interface Climate extends Domain {
        CompletableFuture<List<HAState>> setTemperature(String entityId, String hvacMode, double temperature);

        CompletableFuture<List<HAState>> setHvacMode(String entityId, String hvacMode);

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

    interface BinarySensor extends Domain {
        default CompletableFuture<BinaryState> getBinaryState(String domainlessEntityId) {
            return getState(domainlessEntityId).thenApply(haState -> BinaryState.valueOf(haState.state().toUpperCase()));
        }

        enum BinaryState {
            ON, OFF, UNAVAILABLE, UNKNOWN
        }
    }

    interface LogBook {
        CompletableFuture<List<HALogbookEntry>> get(String entityId, Optional<Instant> from, Optional<Instant> to);
    }

    interface History {
        CompletableFuture<Map<String, List<HAHistoryEntry>>> get(List<String> entityIds, Optional<Instant> from, Optional<Instant> to);
    }
}
