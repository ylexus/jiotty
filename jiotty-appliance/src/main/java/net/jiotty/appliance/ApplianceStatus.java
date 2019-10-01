package net.jiotty.appliance;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public enum ApplianceStatus {
    ON {
        @Override
        public boolean matches(Command command) {
            return command == PowerCommand.ON;
        }
    },
    OFF {
        @Override
        public boolean matches(Command command) {
            return command == PowerCommand.OFF;
        }
    },
    IN_TRANSITION {
        @Override
        public boolean matches(Command command) {
            return false;
        }
    };

    private static final Map<PowerCommand, ApplianceStatus> COMMAND_TO_STATUS = ImmutableMap.of(PowerCommand.ON, ON, PowerCommand.OFF, OFF);

    public static ApplianceStatus forCommand(Command command) {
        return command.acceptOrFail((PowerCommand.Visitor<ApplianceStatus>) COMMAND_TO_STATUS::get);
    }

    public abstract boolean matches(Command command);
}
