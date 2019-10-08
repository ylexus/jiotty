package net.yudichev.jiotty.connector.aws.iot.mqtt;

import net.yudichev.jiotty.common.lang.Closeable;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface AwsIotMqttConnection {
    <T> CompletableFuture<Closeable> subscribe(String topic, Class<T> payloadType, BiConsumer<? super String, ? super T> callback);
}
