package net.jiotty.connector.mqtt;

import com.google.inject.BindingAnnotation;
import net.jiotty.common.inject.BaseLifecycleComponent;
import net.jiotty.common.lang.Closeable;
import net.jiotty.common.lang.DeduplicatingBiConsumer;
import net.jiotty.common.lang.throttling.ThresholdThrottlingConsumerFactory;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static net.jiotty.common.lang.Closeable.idempotent;
import static net.jiotty.common.lang.MoreThrowables.asUnchecked;
import static net.jiotty.common.lang.Runnables.guarded;

final class MqttImpl extends BaseLifecycleComponent implements Mqtt {
    private static final Logger logger = LoggerFactory.getLogger(MqttImpl.class);
    private final ThresholdThrottlingConsumerFactory<Throwable> throttledLoggerFactory;
    private final MqttConnectOptions mqttConnectOptions;
    private final Object lock = new Object();
    private final Map<String, MqttMessage> lastReceivedMessageByTopic = new HashMap<>();
    private final Map<String, Set<BiConsumer<String, MqttMessage>>> subscriptionsByFilter = new HashMap<>();
    private final IMqttClient client;

    @Inject
    MqttImpl(IMqttClient client,
             @Dependency ThresholdThrottlingConsumerFactory<Throwable> throttledLoggerFactory,
             @Dependency MqttConnectOptions mqttConnectOptions) {
        this.throttledLoggerFactory = checkNotNull(throttledLoggerFactory);
        this.mqttConnectOptions = checkNotNull(mqttConnectOptions);
        this.client = checkNotNull(client);
    }

    @Override
    public Closeable subscribe(String topicFilter, BiConsumer<String, String> dataCallback) {
        checkStarted();
        BiConsumer<String, MqttMessage> callback = exceptionLogging(new MessageToStringDataCallback(new DeduplicatingBiConsumer<>(dataCallback)));

        synchronized (lock) {
            deliverLastMessages(topicFilter, callback);

            doSubscribe(topicFilter, callback);
            subscriptionsByFilter.computeIfAbsent(topicFilter, ignored -> new HashSet<>()).add(callback);
        }
        return idempotent(() -> asUnchecked(() -> {
            synchronized (lock) {
                if (client.isConnected()) {
                    client.unsubscribe(topicFilter);
                }
                subscriptionsByFilter.compute(topicFilter, (filter, callbacks) -> {
                    checkNotNull(callbacks).remove(callback);
                    if (callbacks.isEmpty()) {
                        return null;
                    }
                    return callbacks;
                });
            }
        }));
    }

    @Override
    public CompletableFuture<Void> publish(String topic, String message) {
        return supplyAsync(() -> {
            synchronized (lock) {
                logger.debug("OUT topic: {}, msg: {}", topic, message);
                asUnchecked(() -> client.publish(topic, message.getBytes(UTF_8), 1, false));
            }
            return null;
        });
    }

    @Override
    protected void doStart() {
        client.setCallback(new ResubscribeOnReconnectCallback());
        asUnchecked(() -> client.connect(mqttConnectOptions));
        logger.info("Connected to broker");
    }

    @Override
    protected void doStop() {
        synchronized (lock) {
            asUnchecked(client::disconnect);
        }
    }

    private <T, U> BiConsumer<T, U> exceptionLogging(BiConsumer<T, U> messageToStringDataCallback) {
        return (t, u) -> guarded(logger, "handling message", () -> messageToStringDataCallback.accept(t, u)).run();
    }

    private void doSubscribe(String topicFilter, BiConsumer<String, MqttMessage> callback) {
        asUnchecked(() -> client.subscribe(topicFilter, (topic, message) -> {
            logger.debug("IN topic: {}, msg: {}", topic, message);
            callback.accept(topic, message);
        }));
    }

    private void deliverLastMessages(String topicFilter, BiConsumer<String, MqttMessage> callback) {
        lastReceivedMessageByTopic.forEach((topic, message) -> {
            if (MqttTopic.isMatched(topicFilter, topic)) {
                logger.debug("Delivering last known message {} -> {}", topic, message);
                guarded(logger, "deliver last known message", () -> callback.accept(topic, message)).run();
            }
        });
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }

    private static class MessageToStringDataCallback implements BiConsumer<String, MqttMessage> {
        private final BiConsumer<String, String> delegate;

        MessageToStringDataCallback(BiConsumer<String, String> delegate) {
            this.delegate = checkNotNull(delegate);
        }

        @Override
        public void accept(String topic, MqttMessage message) {
            delegate.accept(topic, new String(message.getPayload(), UTF_8));
        }
    }

    private class ResubscribeOnReconnectCallback implements MqttCallbackExtended {
        private final Consumer<Throwable> throttledErrorLogger = throttledLoggerFactory.create(5, Duration.ofMinutes(1), e ->
                logger.error("{} lost connection to {} too often (suppressing this error for 1 minute)", client.getClientId(), client.getServerURI(), e));

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            synchronized (lock) {
                logger.debug("connectComplete: reconnect={}", reconnect);
                if (reconnect) {
                    logger.info("Re-connected, restoring subscriptions: {}", subscriptionsByFilter);
                    guarded(logger, "re-subscribing to mqtt",
                            () -> subscriptionsByFilter.forEach((topicFilter, callbacks) ->
                                    callbacks.forEach(callback -> doSubscribe(topicFilter, callback))))
                            .run();
                }
            }
        }

        @Override
        public void connectionLost(Throwable cause) {
            throttledErrorLogger.accept(cause);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            synchronized (lock) {
                logger.debug("messageArrived: {}->{}", topic, message);
                lastReceivedMessageByTopic.put(topic, message);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            logger.debug("Message delivered: {}", token.getMessageId());
        }
    }
}
