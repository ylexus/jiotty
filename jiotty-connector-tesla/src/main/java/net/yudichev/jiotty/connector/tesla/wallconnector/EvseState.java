package net.yudichev.jiotty.connector.tesla.wallconnector;

import static com.google.common.base.Preconditions.checkArgument;

public enum EvseState {
    BOOTING(0),
    NOT_CONNECTED(1),
    CONNECTED(2),
    READY(4),
    NEGOTIATING(6),
    ERROR(7),
    CHARGING_FINISHED(8),
    WAITING_CAR(9),
    CHARGING_REDUCED(10),
    CHARGING(11),
    ;

    private static final EvseState[] VALUES_BY_ID;

    static {
        VALUES_BY_ID = new EvseState[12];
        for (EvseState state : values()) {
            VALUES_BY_ID[state.id] = state;
        }
    }

    private final int id;

    EvseState(int id) {
        this.id = id;
    }

    public static EvseState forId(int id) {
        checkArgument(id >= 0 && id < VALUES_BY_ID.length && VALUES_BY_ID[id] != null, "Invalid EVSE state ID: %s", id);
        return VALUES_BY_ID[id];
    }
}
