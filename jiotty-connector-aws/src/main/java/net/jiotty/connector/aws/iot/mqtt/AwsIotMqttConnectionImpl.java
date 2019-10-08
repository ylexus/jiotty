package net.jiotty.connector.aws.iot.mqtt;

import com.amazonaws.services.iot.client.*;
import com.google.common.collect.Sets;
import com.google.inject.BindingAnnotation;
import net.jiotty.common.inject.BaseLifecycleComponent;
import net.jiotty.common.lang.Closeable;
import net.jiotty.common.lang.Json;
import net.jiotty.connector.aws.PrivateKeyReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.io.Resources.getResource;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.jiotty.common.lang.Closeable.idempotent;
import static net.jiotty.common.lang.MoreThrowables.asUnchecked;

final class AwsIotMqttConnectionImpl extends BaseLifecycleComponent implements AwsIotMqttConnection {
    private static final Logger logger = LoggerFactory.getLogger(AwsIotMqttConnectionImpl.class);

    private final Duration timeout;
    private final AWSIotMqttClient client;
    private final Set<String> subscribedTopics = Sets.newConcurrentHashSet();
    private final Object subscriptionLock = new Object();

    @Inject
    AwsIotMqttConnectionImpl(@ClientEndpoint String clientEndpoint,
                             @ClientId String clientId,
                             @Timeout Duration timeout) {
        this.timeout = checkNotNull(timeout);
        try {
            PrivateKey privateKey;
            try (InputStream stream = getResource("aws/private.key").openStream()) {
                privateKey = PrivateKeyReader.getPrivateKey(stream, null);
            }
            Collection<? extends Certificate> certificates;
            try (InputStream stream = getResource("aws/cert.pem").openStream()) {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                certificates = certFactory.generateCertificates(stream);
            }
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);

            // randomly generated key password for the key in the KeyStore
            String keyPassword = new BigInteger(128, new SecureRandom()).toString(32);

            Certificate[] certChain = new Certificate[certificates.size()];
            certChain = certificates.toArray(certChain);
            keyStore.setKeyEntry("alias", privateKey, keyPassword.toCharArray(), certChain);

            client = new AWSIotMqttClient(clientEndpoint, clientId, keyStore, keyPassword);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> CompletableFuture<Closeable> subscribe(String topic, Class<T> payloadType, BiConsumer<? super String, ? super T> callback) {
        synchronized (subscriptionLock) {
            checkStarted();
            checkState(subscribedTopics.add(topic), "already subscribed to topic %s", topic);
            CompletableFuture<Closeable> result = new CompletableFuture<>();
            try {
                client.subscribe(new AWSIotTopic(topic, AWSIotQos.QOS1) {
                    @SuppressWarnings("AmbiguousFieldAccess") // it's one and the same
                    @Override
                    public void onSuccess() {
                        logger.info("Subscribed to {}", topic);
                        result.complete(idempotent(() -> asUnchecked(() -> {
                            try {
                                client.unsubscribe(topic, timeout.toMillis());
                            } finally {
                                subscribedTopics.remove(topic);
                            }
                        })));
                    }

                    @Override
                    public void onTimeout() {
                        onSubscriptionFailure("timed out");
                    }

                    @Override
                    public void onFailure() {
                        onSubscriptionFailure("failed");
                    }

                    @Override
                    public void onMessage(AWSIotMessage message) {
                        String stringPayload = message.getStringPayload();
                        T parsedPayload = null;
                        try {
                            parsedPayload = Json.parse(stringPayload, payloadType);
                        } catch (RuntimeException e) {
                            logger.error("Unable to parse payload as {}: {}", payloadType, stringPayload, e);
                        }
                        if (parsedPayload != null) {
                            try {
                                callback.accept(message.getTopic(), parsedPayload);
                            } catch (RuntimeException e) {
                                logger.error("Message handler failure", e);
                            }
                        }
                    }

                    @SuppressWarnings("AmbiguousFieldAccess") // it's one and the same
                    private void onSubscriptionFailure(String reason) {
                        subscribedTopics.remove(topic);
                        result.completeExceptionally(new RuntimeException("Subscription " + reason));
                    }
                });
            } catch (AWSIotException e) {
                result.completeExceptionally(e);
            }
            return result;
        }
    }

    @Override
    public void doStart() {
        asUnchecked(() -> client.connect(10_000L));
    }

    @Override
    protected void doStop() {
        asUnchecked(() -> client.disconnect(10_000L));
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface ClientEndpoint {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface ClientId {
    }


    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Timeout {
    }
}
