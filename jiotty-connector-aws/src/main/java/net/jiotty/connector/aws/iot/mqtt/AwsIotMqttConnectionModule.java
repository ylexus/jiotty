package net.jiotty.connector.aws.iot.mqtt;

import net.jiotty.common.inject.BaseLifecycleComponentModule;
import net.jiotty.common.inject.ExposedKeyModule;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;

public final class AwsIotMqttConnectionModule extends BaseLifecycleComponentModule implements ExposedKeyModule<AwsIotMqttConnection> {
    private final String clientId;
    private final String clientEndpoint;
    private final Duration timeout;

    public AwsIotMqttConnectionModule(String clientId, String clientEndpoint, Duration timeout) {
        this.clientId = checkNotNull(clientId);
        this.clientEndpoint = checkNotNull(clientEndpoint);
        this.timeout = checkNotNull(timeout);
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(AwsIotMqttConnectionImpl.ClientId.class).to(clientId);
        bindConstant().annotatedWith(AwsIotMqttConnectionImpl.ClientEndpoint.class).to(clientEndpoint);
        bind(Duration.class).annotatedWith(AwsIotMqttConnectionImpl.Timeout.class).toInstance(timeout);

        bind(getExposedKey()).to(boundLifecycleComponent(AwsIotMqttConnectionImpl.class));
        expose(getExposedKey());
    }
}