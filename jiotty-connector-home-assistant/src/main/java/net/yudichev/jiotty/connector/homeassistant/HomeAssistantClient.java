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

    Domain<Void> sensor();

    LogBook logBook();

    History history();

    BinarySensor binarySensor();

    Domain<HADeviceLocationAttributes> deviceTracker();

    interface Domain<A> {
        CompletableFuture<HAState<A>> getState(String domainlessEntityId);
    }

    interface Climate extends Domain<Void> {
        CompletableFuture<List<HAState<Void>>> setTemperature(String domainlessEntityId, String hvacMode, double temperature);

        CompletableFuture<List<HAState<Void>>> setHvacMode(String domainlessEntityId, String hvacMode);

        CompletableFuture<List<HAState<Void>>> turnOn(String domainlessEntityId);

        CompletableFuture<List<HAState<Void>>> turnOff(String domainlessEntityId);
    }

    interface Switch extends Domain<Void> {
        CompletableFuture<List<HAState<Void>>> turnOn(String domainlessEntityId);

        CompletableFuture<List<HAState<Void>>> turnOff(String domainlessEntityId);
    }

    interface Number extends Domain<Void> {
        CompletableFuture<List<HAState<Void>>> setValue(String domainlessEntityId, double value);
    }

    interface Button extends Domain<Void> {
        CompletableFuture<List<HAState<Void>>> press(String domainlessEntityId);
    }

    interface BinarySensor extends Domain<Void> {
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
