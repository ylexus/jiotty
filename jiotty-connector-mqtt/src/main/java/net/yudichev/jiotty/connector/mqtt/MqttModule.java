package net.yudichev.jiotty.connector.mqtt;

import com.google.inject.Key;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.inject.HasWithAnnotation;
import net.yudichev.jiotty.common.inject.SpecifiedAnnotation;
import net.yudichev.jiotty.common.lang.TypedBuilder;
import net.yudichev.jiotty.common.lang.throttling.ThresholdThrottlingConsumerModule;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import javax.inject.Singleton;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.SpecifiedAnnotation.forAnnotation;
import static net.yudichev.jiotty.common.inject.SpecifiedAnnotation.forNoAnnotation;

public final class MqttModule extends BaseLifecycleComponentModule implements ExposedKeyModule<Mqtt> {
    private final String serverUri;
    private final String clientId;
    private final Consumer<MqttConnectOptions> connectionOptionsCustomiser;
    private final Key<Mqtt> exposedKey;

    private MqttModule(String serverUri,
                       String clientId,
                       Consumer<MqttConnectOptions> connectionOptionsCustomiser,
                       SpecifiedAnnotation specifiedAnnotation) {
        this.serverUri = checkNotNull(serverUri);
        this.clientId = checkNotNull(clientId);
        this.connectionOptionsCustomiser = checkNotNull(connectionOptionsCustomiser);
        exposedKey = specifiedAnnotation.specify(ExposedKeyModule.super.getExposedKey().getTypeLiteral());
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Key<Mqtt> getExposedKey() {
        return exposedKey;
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
                .withAnnotation(forAnnotation(MqttImpl.Dependency.class))
                .build());

        bind(exposedKey).to(registerLifecycleComponent(MqttImpl.class));
        expose(exposedKey);
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<Mqtt>>, HasWithAnnotation {
        private String serverUri;
        private String clientId;
        private Consumer<MqttConnectOptions> connectionOptionsCustomiser = ignored -> {};
        private SpecifiedAnnotation specifiedAnnotation = forNoAnnotation();

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
        public Builder withAnnotation(SpecifiedAnnotation specifiedAnnotation) {
            this.specifiedAnnotation = checkNotNull(specifiedAnnotation);
            return this;
        }

        @Override
        public ExposedKeyModule<Mqtt> build() {
            return new MqttModule(serverUri, clientId, connectionOptionsCustomiser, specifiedAnnotation);
        }
    }
}
