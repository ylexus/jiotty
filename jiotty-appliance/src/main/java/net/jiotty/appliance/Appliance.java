package net.jiotty.appliance;

import net.jiotty.common.lang.HasName;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface Appliance extends HasName {
    CompletableFuture<?> execute(Command command);

    Set<? extends Command> getAllSupportedCommands();

    default CompletableFuture<?> turnOn() {
        return execute(PowerCommand.ON);
    }

    default CompletableFuture<?> turnOff() {
        return execute(PowerCommand.OFF);
    }
}
