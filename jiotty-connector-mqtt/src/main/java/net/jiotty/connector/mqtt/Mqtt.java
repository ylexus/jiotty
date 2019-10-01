package net.jiotty.connector.mqtt;

import net.jiotty.common.lang.Closeable;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface Mqtt {
    Closeable subscribe(String topicFilter, BiConsumer<String, String> dataCallback);

    CompletableFuture<Void> publish(String topic, String message);
}
