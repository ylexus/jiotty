package net.yudichev.jiotty.connector.aws.iot.mqtt;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;

public final class AwsIotMqttConnectionModule extends BaseLifecycleComponentModule implements ExposedKeyModule<AwsIotMqttConnection> {
    private final String clientId;
    private final String clientEndpoint;
    private final Duration timeout;

    private AwsIotMqttConnectionModule(String clientId, String clientEndpoint, Duration timeout) {
        this.clientId = checkNotNull(clientId);
        this.clientEndpoint = checkNotNull(clientEndpoint);
        this.timeout = checkNotNull(timeout);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(AwsIotMqttConnectionImpl.ClientId.class).to(clientId);
        bindConstant().annotatedWith(AwsIotMqttConnectionImpl.ClientEndpoint.class).to(clientEndpoint);
        bind(Duration.class).annotatedWith(AwsIotMqttConnectionImpl.Timeout.class).toInstance(timeout);

        bind(getExposedKey()).to(boundLifecycleComponent(AwsIotMqttConnectionImpl.class));
        expose(getExposedKey());
    }

    public static class Builder implements TypedBuilder<ExposedKeyModule<AwsIotMqttConnection>> {
        private String clientId;
        private String clientEndpoint;
        private Duration timeout;

        public Builder setClientId(String clientId) {
            this.clientId = checkNotNull(clientId);
            return this;
        }

        public Builder setClientEndpoint(String clientEndpoint) {
            this.clientEndpoint = checkNotNull(clientEndpoint);
            return this;
        }

        public Builder setTimeout(Duration timeout) {
            this.timeout = checkNotNull(timeout);
            return this;
        }

        @Override
        public ExposedKeyModule<AwsIotMqttConnection> build() {
            return new AwsIotMqttConnectionModule(clientId, clientEndpoint, timeout);
        }
    }
}