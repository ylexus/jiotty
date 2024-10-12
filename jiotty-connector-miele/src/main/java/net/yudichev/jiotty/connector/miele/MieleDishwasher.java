package net.yudichev.jiotty.connector.miele;

import net.yudichev.jiotty.common.lang.Closeable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface MieleDishwasher {
    Closeable subscribeToEvents(Consumer<? super MieleEvent> eventHandler);

    CompletableFuture<List<MieleProgram>> getPrograms();

    CompletableFuture<MieleActions> getActions();

    CompletableFuture<Void> startProgram(int programId);

    CompletableFuture<Void> powerOn();

    CompletableFuture<Void> powerOff();
}
