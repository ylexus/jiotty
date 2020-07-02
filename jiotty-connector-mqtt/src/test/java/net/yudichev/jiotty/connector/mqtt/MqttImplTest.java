package net.yudichev.jiotty.connector.mqtt;

import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.lang.Closeable;
import org.eclipse.paho.client.mqttv3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.BiConsumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MqttImplTest {
    private static final String TOPIC_FILTER = "/topic/+";
    @Mock
    private IMqttClient client;
    @Mock
    private BiConsumer<String, String> dataCallback;
    @Mock
    private SchedulingExecutor executor;
    @Captor
    private ArgumentCaptor<IMqttMessageListener> messageListenerArgumentCaptor;
    private MqttImpl mqtt;
    private MqttCallbackExtended mqttCallback;

    @BeforeEach
    void setUp() throws MqttException {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqtt = new MqttImpl(client, threadNameBase -> executor, (threshold, throttlingDuration, delegate) -> e -> {}, mqttConnectOptions);
        mqtt.start();

        ArgumentCaptor<MqttCallbackExtended> callbackCaptor = ArgumentCaptor.forClass(MqttCallbackExtended.class);
        verify(client).setCallback(callbackCaptor.capture());
        mqttCallback = callbackCaptor.getValue();
        verify(client).connect(mqttConnectOptions);
    }

    @Test
    void resubscribesOnReconnect() throws MqttException {
        IMqttMessageListener messageListener = doSubscribe();

        mqttCallback.connectionLost(new RuntimeException("oops"));
        mqttCallback.connectComplete(true, "serverUri");

        verify(client).subscribe(TOPIC_FILTER, messageListener);
    }

    @Test
    void deliversLastMatchingMessageOnSubscribe() throws Exception {
        mqttCallback.messageArrived("/topic/a", mqttMessage());
        mqttCallback.messageArrived("/topic2/b", mqttMessage());

        doSubscribe();

        verify(dataCallback).accept("/topic/a", "msg");
    }

    @Test
    void multipleSubscriptionsToSameTopic(@Mock BiConsumer<String, String> dataCallback2) throws Exception {
        when(client.isConnected()).thenReturn(true);

        mqttCallback.connectComplete(false, "serverUrl");
        Closeable sub1 = mqtt.subscribe(TOPIC_FILTER, dataCallback);

        verify(client).subscribe(eq(TOPIC_FILTER), messageListenerArgumentCaptor.capture());
        IMqttMessageListener mqttListener = messageListenerArgumentCaptor.getValue();

        Closeable sub2 = mqtt.subscribe(TOPIC_FILTER, dataCallback2);
        verify(client, times(1)).subscribe(eq(TOPIC_FILTER), any());

        mqttListener.messageArrived("/topic/a", mqttMessage("msg"));
        verify(dataCallback).accept("/topic/a", "msg");
        verify(dataCallback2).accept("/topic/a", "msg");

        sub1.close();
        verify(client, never()).unsubscribe(TOPIC_FILTER);

        mqttListener.messageArrived("/topic/a", mqttMessage("msg2"));
        verify(dataCallback, never()).accept("/topic/a", "msg2");
        verify(dataCallback2).accept("/topic/a", "msg2");

        sub2.close();
        verify(client).unsubscribe(TOPIC_FILTER);
    }

    private IMqttMessageListener doSubscribe() throws MqttException {
        mqttCallback.connectComplete(false, "serverUrl");
        mqtt.subscribe(TOPIC_FILTER, dataCallback);

        verify(client).subscribe(eq(TOPIC_FILTER), messageListenerArgumentCaptor.capture());
        return messageListenerArgumentCaptor.getValue();
    }

    private static MqttMessage mqttMessage() {
        return mqttMessage("msg");
    }

    private static MqttMessage mqttMessage(String msg) {
        return new MqttMessage(msg.getBytes(UTF_8));
    }
}