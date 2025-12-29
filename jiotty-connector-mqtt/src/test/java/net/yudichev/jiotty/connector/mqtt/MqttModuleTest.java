package net.yudichev.jiotty.connector.mqtt;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import net.yudichev.jiotty.common.async.ExecutorModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.time.TimeModule;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static net.yudichev.jiotty.common.inject.SpecifiedAnnotation.forAnnotation;
import static org.hamcrest.MatcherAssert.assertThat;

class MqttModuleTest {
    @Test
    void injector() {
        Named annotation = Names.named("a");
        ExposedKeyModule<Mqtt> module = MqttModule.builder()
                                                  .setClientId("ci")
                                                  .setServerUri("su")
                                                  .withConnectionOptionsCustomised(BindingSpec.literally(options -> options.setUserName("u")))
                                                  .withAnnotation(forAnnotation(annotation))
                                                  .build();

        assertThat(module.getExposedKey(), Matchers.is(Key.get(Mqtt.class, annotation)));

        Guice.createInjector(new TimeModule(), new ExecutorModule(), module).getBinding(module.getExposedKey());
    }
}