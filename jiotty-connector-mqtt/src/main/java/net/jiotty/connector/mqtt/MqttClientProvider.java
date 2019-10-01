package net.jiotty.connector.mqtt;

import com.google.inject.BindingAnnotation;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.jiotty.common.lang.MoreThrowables.getAsUnchecked;

final class MqttClientProvider implements Provider<MqttClient> {
    private static final Logger logger = LoggerFactory.getLogger(MqttClientProvider.class);
    private final String serverUri;
    private final String clientId;

    @Inject
    MqttClientProvider(@ServerUri String serverUri,
                       @ClientId String clientId) {
        this.serverUri = checkNotNull(serverUri);
        this.clientId = checkNotNull(clientId);
    }

    @Override
    public MqttClient get() {
        logger.info("Creating MQTT client for {} as {}", serverUri, clientId);
        return getAsUnchecked(() -> new MqttClient(serverUri, clientId, new MemoryPersistence()));
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface ServerUri {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface ClientId {
    }
}
