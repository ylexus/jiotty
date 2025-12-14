package net.yudichev.jiotty.connector.mqtt.presence;

import net.yudichev.jiotty.common.lang.Closeable;

import java.util.function.Consumer;

/** Allows for subscribing to the presence or absence of regular updates on an MQTT topic. */
public interface MqttPresenceService {
    Closeable addListener(Consumer<Boolean> listener);
}
