package net.yudichev.jiotty.connector.google.assistant;

import java.util.concurrent.CompletableFuture;

public interface GoogleAssistantClient {
    CompletableFuture<byte[]> assist(String phrase);
}
