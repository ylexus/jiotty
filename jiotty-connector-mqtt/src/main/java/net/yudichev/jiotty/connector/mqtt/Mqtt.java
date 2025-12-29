package net.yudichev.jiotty.connector.mqtt;

import net.yudichev.jiotty.common.lang.Closeable;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface Mqtt {
    Closeable subscribe(String topicFilter, int qos, BiConsumer<String, String> dataCallback);

    default Closeable subscribe(String topicFilter, BiConsumer<String, String> dataCallback) {
        return subscribe(topicFilter, 2, dataCallback);
    }

    CompletableFuture<Void> publish(String topic, String message);
}
