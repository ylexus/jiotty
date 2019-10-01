package net.jiotty.connector.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.BiConsumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MqttImplTest {
    private static final String TOPIC_FILTER = "/topic/+";
    @Mock
    private IMqttClient client;
    @Mock
    private BiConsumer<String, String> dataCallback;
    private MqttImpl mqtt;
    private MqttCallbackExtended mqttCallback;

    @BeforeEach
    void setUp() throws MqttException {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqtt = new MqttImpl(client, (threshold, throttlingDuration, delegate) -> e -> {}, mqttConnectOptions);
        mqtt.start();

        ArgumentCaptor<MqttCallbackExtended> callbackCaptor = ArgumentCaptor.forClass(MqttCallbackExtended.class);
        verify(client).setCallback(callbackCaptor.capture());
        mqttCallback = callbackCaptor.getValue();
        verify(client).connect(mqttConnectOptions);
    }

    @Test
    void resubscribesOnReconnect() throws Exception {
        IMqttMessageListener messageListener = doSubscribe();

        mqttCallback.connectionLost(new RuntimeException("oops"));
        mqttCallback.connectComplete(true, "serverUri");

        verify(client).subscribe(TOPIC_FILTER, messageListener);
    }

    @Test
    void deliversLastMatchingMessageOnSubscribe() throws Exception {
        MqttMessage message = new MqttMessage("msg".getBytes(UTF_8));
        mqttCallback.messageArrived("/topic/a", message);
        mqttCallback.messageArrived("/topic2/b", message);

        doSubscribe();

        verify(dataCallback).accept("/topic/a", "msg");
    }

    private IMqttMessageListener doSubscribe() throws MqttException {
        mqttCallback.connectComplete(false, "serverUrl");
        mqtt.subscribe(TOPIC_FILTER, dataCallback);

        ArgumentCaptor<IMqttMessageListener> messageListenerArgumentCaptor = ArgumentCaptor.forClass(IMqttMessageListener.class);
        verify(client).subscribe(eq(TOPIC_FILTER), messageListenerArgumentCaptor.capture());
        return messageListenerArgumentCaptor.getValue();
    }
}