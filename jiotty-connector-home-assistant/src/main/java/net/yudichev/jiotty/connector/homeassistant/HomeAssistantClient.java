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
        CompletableFuture<List<HAState>> setTemperature(String domainlessEntityId, String hvacMode, double temperature);

        CompletableFuture<List<HAState>> setHvacMode(String domainlessEntityId, String hvacMode);

        CompletableFuture<List<HAState>> turnOn(String domainlessEntityId);

        CompletableFuture<List<HAState>> turnOff(String domainlessEntityId);
    }

    interface Switch extends Domain {
        CompletableFuture<List<HAState>> turnOn(String domainlessEntityId);

        CompletableFuture<List<HAState>> turnOff(String domainlessEntityId);
    }

    interface Number extends Domain {
        CompletableFuture<List<HAState>> setValue(String domainlessEntityId, double value);
    }

    interface Button extends Domain {
        CompletableFuture<List<HAState>> press(String domainlessEntityId);
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
