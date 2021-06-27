package net.yudichev.jiotty.appliance;

import net.yudichev.jiotty.common.lang.HasName;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface Appliance extends HasName {
    CompletableFuture<?> execute(Command<?> command);

    Set<CommandMeta<?>> getAllSupportedCommandMetadata();

    default CompletableFuture<?> turnOn() {
        return execute(PowerCommand.ON);
    }

    default CompletableFuture<?> turnOff() {
        return execute(PowerCommand.OFF);
    }
}
