package net.yudichev.jiotty.connector.mqtt.presence;

import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.connector.mqtt.Mqtt;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public final class MqttPresenceServiceModule extends BaseLifecycleComponentModule implements ExposedKeyModule<MqttPresenceService> {
    private final BindingSpec<String> nameSpec;
    private final BindingSpec<Mqtt> mqttSpec;
    private final BindingSpec<String> bleAdvertMqttTopicSpec;

    public MqttPresenceServiceModule(BindingSpec<String> nameSpec,
                                     BindingSpec<Mqtt> mqttSpec,
                                     BindingSpec<String> bleAdvertMqttTopicSpec) {
        this.nameSpec = checkNotNull(nameSpec);
        this.mqttSpec = checkNotNull(mqttSpec);
        this.bleAdvertMqttTopicSpec = checkNotNull(bleAdvertMqttTopicSpec);
    }

    @Override
    protected void configure() {
        mqttSpec.bind(Mqtt.class).annotatedWith(Dependency.class).installedBy(this::installLifecycleComponentModule);
        bleAdvertMqttTopicSpec.bind(String.class).annotatedWith(MqttTopic.class).installedBy(this::installLifecycleComponentModule);
        nameSpec.bind(String.class).annotatedWith(Name.class).installedBy(this::installLifecycleComponentModule);
        bind(getExposedKey()).to(registerLifecycleComponent(MqttPresenceServiceImpl.class));
        expose(getExposedKey());
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface MqttTopic {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Name {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }

}
