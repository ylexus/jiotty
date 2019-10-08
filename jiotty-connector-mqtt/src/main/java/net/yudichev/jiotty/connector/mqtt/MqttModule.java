package net.yudichev.jiotty.connector.mqtt;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.inject.SpecifiedAnnotation;
import net.yudichev.jiotty.common.lang.TypedBuilder;
import net.yudichev.jiotty.common.lang.throttling.ThresholdThrottlingConsumerModule;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import javax.inject.Singleton;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public final class MqttModule extends BaseLifecycleComponentModule implements ExposedKeyModule<Mqtt> {
    private final String serverUri;
    private final String clientId;
    private final Consumer<MqttConnectOptions> connectionOptionsCustomiser;

    private MqttModule(String serverUri, String clientId, Consumer<MqttConnectOptions> connectionOptionsCustomiser) {
        this.serverUri = checkNotNull(serverUri);
        this.clientId = checkNotNull(clientId);
        this.connectionOptionsCustomiser = checkNotNull(connectionOptionsCustomiser);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(MqttClientProvider.ServerUri.class).to(serverUri);
        bindConstant().annotatedWith(MqttClientProvider.ClientId.class).to(clientId);
        bind(IMqttClient.class).toProvider(MqttClientProvider.class).in(Singleton.class);

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        connectionOptionsCustomiser.accept(mqttConnectOptions);
        bind(MqttConnectOptions.class).annotatedWith(MqttImpl.Dependency.class).toInstance(mqttConnectOptions);

        installLifecycleComponentModule(ThresholdThrottlingConsumerModule.builder()
                .setValueType(Throwable.class)
                .withAnnotation(SpecifiedAnnotation.forAnnotation(MqttImpl.Dependency.class))
                .build());

        bind(getExposedKey()).to(boundLifecycleComponent(MqttImpl.class));
        expose(getExposedKey());
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<Mqtt>> {
        private String serverUri;
        private String clientId;
        private Consumer<MqttConnectOptions> connectionOptionsCustomiser = ignored -> {};

        public Builder setServerUri(String serverUri) {
            this.serverUri = checkNotNull(serverUri);
            return this;
        }

        public Builder setClientId(String clientId) {
            this.clientId = checkNotNull(clientId);
            return this;
        }

        public Builder withConnectionOptionsCustomised(Consumer<MqttConnectOptions> connectionOptionsCustomiser) {
            this.connectionOptionsCustomiser = checkNotNull(connectionOptionsCustomiser);
            return this;
        }

        @Override
        public ExposedKeyModule<Mqtt> build() {
            return new MqttModule(serverUri, clientId, connectionOptionsCustomiser);
        }
    }
}
