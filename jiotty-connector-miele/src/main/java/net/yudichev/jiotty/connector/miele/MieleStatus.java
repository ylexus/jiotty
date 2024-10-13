package net.yudichev.jiotty.connector.miele;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

public enum MieleStatus {
    OFF(1),
    ON(2),
    PROGRAMMED(3),
    PROGRAMMED_WAITING_TO_START(4),
    RUNNING(5),
    PAUSE(6),
    END_PROGRAMMED(7),
    FAILURE(8),
    PROGRAMME_INTERRUPTED(9),
    IDLE(10),
    RINSE_HOLD(11),
    SERVICE(12),
    SUPERFREEZING(13),
    SUPERCOOLING(14),
    SUPERHEATING(15),
    SUPERCOOLING_SUPERFREEZING(146),
    NOT_CONNECTED(255),
    ;

    private static final Map<Integer, MieleStatus> idByStatus = Stream.of(values()).collect(toImmutableMap(MieleStatus::getId, Function.identity()));

    private final int id;

    MieleStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static MieleStatus forId(int id) {
        var status = idByStatus.get(id);
        checkArgument(status != null, "Unknown id: " + id);
        return status;
    }
}
