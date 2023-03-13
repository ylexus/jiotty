package net.yudichev.jiotty.connector.mqtt;

import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.common.async.AsyncOperationFailureHandler;
import net.yudichev.jiotty.common.async.AsyncOperationRetry;
import net.yudichev.jiotty.common.async.AsyncOperationRetryImpl;
import net.yudichev.jiotty.common.async.ExecutorFactory;
import net.yudichev.jiotty.common.async.Scheduler;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.Closeable;
import net.yudichev.jiotty.common.lang.DeduplicatingBiConsumer;
import net.yudichev.jiotty.common.lang.backoff.BackOff;
import net.yudichev.jiotty.common.lang.backoff.ExponentialBackOff;
import net.yudichev.jiotty.common.lang.backoff.NanoClock;
import net.yudichev.jiotty.common.lang.backoff.SynchronizedBackOff;
import net.yudichev.jiotty.common.lang.throttling.ThresholdThrottlingConsumerFactory;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
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
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static net.yudichev.jiotty.common.lang.Closeable.closeIfNotNull;
import static net.yudichev.jiotty.common.lang.Closeable.closeSafelyIfNotNull;
import static net.yudichev.jiotty.common.lang.Closeable.idempotent;
import static net.yudichev.jiotty.common.lang.Closeable.noop;
import static net.yudichev.jiotty.common.lang.CompositeException.runForAll;
import static net.yudichev.jiotty.common.lang.HumanReadableExceptionMessage.humanReadableMessage;
import static net.yudichev.jiotty.common.lang.MoreThrowables.asUnchecked;
import static net.yudichev.jiotty.common.lang.Runnables.guarded;

class MqttImpl extends BaseLifecycleComponent implements Mqtt {
    private static final Logger logger = LoggerFactory.getLogger(MqttImpl.class);
    private final ThresholdThrottlingConsumerFactory<Throwable> throttledLoggerFactory;
    private final MqttConnectOptions mqttConnectOptions;
    private final Map<String, MqttMessage> lastReceivedMessageByTopic = new HashMap<>();
    private final Map<String, Set<BiConsumer<String, MqttMessage>>> subscriptionsByFilter = new HashMap<>();
    private final IMqttAsyncClient client;
    private final ExecutorFactory executorFactory;
    private final String name;
    private final double connectBackoffRandmisationFactor;
    private final NanoClock nanoClock;
    private SchedulingExecutor executor;

    @Inject
    MqttImpl(IMqttAsyncClient client,
             ExecutorFactory executorFactory,
             @Dependency ThresholdThrottlingConsumerFactory<Throwable> throttledLoggerFactory,
             @Dependency MqttConnectOptions mqttConnectOptions) {
        this(client, executorFactory, throttledLoggerFactory, mqttConnectOptions, System::nanoTime, ExponentialBackOff.DEFAULT_RANDOMIZATION_FACTOR);
    }

    MqttImpl(IMqttAsyncClient client,
             ExecutorFactory executorFactory,
             ThresholdThrottlingConsumerFactory<Throwable> throttledLoggerFactory,
             MqttConnectOptions mqttConnectOptions,
             NanoClock nanoClock,
             double connectBackoffRandmisationFactor) {
        this.executorFactory = checkNotNull(executorFactory);
        this.throttledLoggerFactory = checkNotNull(throttledLoggerFactory);
        this.mqttConnectOptions = checkNotNull(mqttConnectOptions);
        this.client = client;
        name = super.name() + " " + client.getClientId() + " " + client.getServerURI();
        this.nanoClock = checkNotNull(nanoClock);
        this.connectBackoffRandmisationFactor = connectBackoffRandmisationFactor;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    protected void doStart() {
        executor = executorFactory.createSingleThreadedSchedulingExecutor("Handler-" + client.getServerURI());
        BackOff backoff = new SynchronizedBackOff(new ExponentialBackOff.Builder()
                .setNanoClock(nanoClock)
                .setInitialIntervalMillis(1000)
                .setMaxIntervalMillis(30_000)
                .setMaxElapsedTimeMillis(Integer.MAX_VALUE)
                .setRandomizationFactor(connectBackoffRandmisationFactor)
                .build());
        AsyncOperationRetry asyncOperationRetry = new AsyncOperationRetryImpl(AsyncOperationFailureHandler.forBackoff(backoff, logger));
        executor.execute(() -> {
            client.setCallback(new ResubscribeOnReconnectCallback());
            CompletableFuture<Void> connectFuture = asyncOperationRetry
                    .withBackOffAndRetry("MQTT Connect to " + client.getServerURI(),
                            () -> {
                                logger.debug("MQTT Connecting to {}", client.getServerURI());
                                var future = new CompletableFuture<Void>();
                                try {
                                    client.connect(mqttConnectOptions, null, new IMqttActionListener() {
                                        @Override
                                        public void onSuccess(IMqttToken asyncActionToken) {
                                            future.complete(null);
                                        }

                                        @Override
                                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                            future.completeExceptionally(exception);
                                        }
                                    });
                                } catch (MqttException e) {
                                    future.completeExceptionally(e);
                                }
                                return future;
                            },
                            (delayMillis, runnable) -> scheduleReconnect(executor, delayMillis, runnable));
            try {
                waitForConnectFutureAndThen(connectFuture, () -> logger.info("Connected to {}", client.getServerURI()));
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                logger.warn("Failed to connect to {}", client.getServerURI(), e);
            }
        });
    }

    // overridable for tests
    void scheduleReconnect(Scheduler scheduler, Long delayMillis, Runnable runnable) {
        // must block the task, so that all user actions queue after start()
        if (delayMillis > 0) {
            asUnchecked(() -> Thread.sleep(delayMillis));
        }
        runnable.run();
    }

    // overridable for tests
    void waitForConnectFutureAndThen(CompletableFuture<Void> connectFuture, Runnable whenDone) throws InterruptedException, ExecutionException {
        // must block the task, so that all user actions queue after start()
        connectFuture.get();
        whenDone.run();
    }

    @Override
    public Closeable subscribe(String topicFilter, BiConsumer<String, String> dataCallback) {
        checkStarted();
        BiConsumer<String, MqttMessage> callback = exceptionLogging(new MessageToStringDataCallback(new DeduplicatingBiConsumer<>(dataCallback)));
        executor.execute(() -> {
            deliverImage(topicFilter, callback);
            subscriptionsByFilter.computeIfAbsent(topicFilter, filter -> {
                doSubscribe(filter, (topic, message) -> runForAll(subscriptionsByFilter.get(filter), consumer -> consumer.accept(topic, message)));
                return new HashSet<>();
            }).add(callback);
        });

        return idempotent(() -> executor.submit(guarded(logger, "unsubscribe", () ->
                subscriptionsByFilter.computeIfPresent(topicFilter, (filter, callbacks) -> {
                    callbacks.remove(callback);
                    if (callbacks.isEmpty()) {
                        if (client.isConnected()) {
                            asUnchecked(() -> client.unsubscribe(topicFilter));
                        }
                        return null;
                    }
                    return callbacks;
                }))));
    }

    @Override
    public CompletableFuture<Void> publish(String topic, String message) {
        checkStarted();
        return supplyAsync(() -> {
            logger.debug("OUT topic: {}, msg: {}", topic, message);
            asUnchecked(() -> client.publish(topic, message.getBytes(UTF_8), 1, false));
            return null;
        }, executor);
    }

    @Override
    protected void doStop() {
        closeIfNotNull(executor);
        // disconnect must not be scheduled to the executor that is potentially blocked on connect; this method also seems to be thread safe
        try {
            client.disconnect();
        } catch (MqttException e) {
            // if the client is already disconnected, disconnect() blows, and we do not care much about it
            logger.info("Failed to disconnect client: {}", humanReadableMessage(e));
        } finally {
            closeSafelyIfNotNull(logger, client); // I have a right as both this component and the client provider are singletons
        }
    }

    private static <T, U> BiConsumer<T, U> exceptionLogging(BiConsumer<T, U> delegate) {
        return (t, u) -> {
            try {
                delegate.accept(t, u);
            } catch (RuntimeException e) {
                logger.error("Error handling message", e);
            }
        };
    }

    private void doSubscribe(String topicFilter, BiConsumer<String, MqttMessage> callback) {
        asUnchecked(() -> client.subscribe(topicFilter, 2, (topic, message) -> {
            logger.debug("IN topic: {}, msg: {}", topic, message);
            guarded(logger, "Notify client on MQTT message", () -> callback.accept(topic, message)).run();
        }));
    }

    private void deliverImage(String topicFilter, BiConsumer<String, MqttMessage> callback) {
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
        private final BackOff backOff = new ExponentialBackOff.Builder()
                .setInitialIntervalMillis(10)
                .setMaxIntervalMillis(10_000)
                .setMultiplier(2)
                .build();
        private Closeable subRetryTimerHandle = noop();

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            logger.info("{} completed connection to {}, reconnected={}", client.getClientId(), serverURI, reconnect);
            if (reconnect) {
                restoreSubscriptions();
            }
        }

        private void restoreSubscriptions() {
            executor.submit(() -> {
                logger.info("Restoring subscriptions: {}", subscriptionsByFilter);
                try {
                    subscriptionsByFilter.forEach((topicFilter, callbacks) ->
                            callbacks.forEach(callback -> doSubscribe(topicFilter, callback)));
                    backOff.reset();
                } catch (RuntimeException e) {
                    long nextRetryInMs = backOff.nextBackOffMillis();
                    logger.info("Re-subscription failed, will re-try in {}ms", nextRetryInMs, e);
                    subRetryTimerHandle = executor.schedule(Duration.ofMillis(nextRetryInMs), this::restoreSubscriptions);
                }
            });
        }

        @Override
        public void connectionLost(Throwable cause) {
            logger.info("{} lost connection to {}", client.getClientId(), client.getServerURI(), cause);
            executor.submit(() -> {
                subRetryTimerHandle.close();
                throttledErrorLogger.accept(cause);
            });
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            executor.submit(() -> {
                logger.debug("messageArrived: {}->{}", topic, message);
                lastReceivedMessageByTopic.put(topic, message);
            });
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            logger.debug("Message delivered: {}", token.getMessageId());
        }
    }
}
