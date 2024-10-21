package net.yudichev.jiotty.connector.tplinksmartplug;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import net.yudichev.jiotty.appliance.Appliance;
import net.yudichev.jiotty.common.async.ExecutorModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import org.junit.jupiter.api.Test;

import static net.yudichev.jiotty.common.inject.BindingSpec.literally;
import static net.yudichev.jiotty.common.inject.SpecifiedAnnotation.forAnnotation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TpLinkSmartPlugModuleTest {
    @Test
    void testCreateInjectorCloud() {
        Named annotation = Names.named("annotation");
        ExposedKeyModule<Appliance> module = TpLinkSmartPlugModule.cloudConnectionBuilder()
                                                                  .withName(literally("name"))
                                                                  .setDeviceId(literally("did"))
                                                                  .setTermId(literally("tid"))
                                                                  .setUsername(literally("u"))
                                                                  .setPassword(literally("p"))
                                                                  .withAnnotation(forAnnotation(annotation))
                                                                  .build();

        assertThat(module.getExposedKey(), is(Key.get(Appliance.class, annotation)));

        Injector injector = Guice.createInjector(module,
                                                 new ExecutorModule());

        injector.getBinding(module.getExposedKey());
    }

    @Test
    void testCreateInjectorLocal() {
        Named annotation = Names.named("annotation");
        ExposedKeyModule<Appliance> module = TpLinkSmartPlugModule.localConnectionBuilder()
                                                                  .withName(literally("name"))
                                                                  .setHost(literally("host"))
                                                                  .withAnnotation(forAnnotation(annotation))
                                                                  .build();

        assertThat(module.getExposedKey(), is(Key.get(Appliance.class, annotation)));

        Injector injector = Guice.createInjector(module,
                                                 new ExecutorModule());

        injector.getBinding(module.getExposedKey());
    }
}