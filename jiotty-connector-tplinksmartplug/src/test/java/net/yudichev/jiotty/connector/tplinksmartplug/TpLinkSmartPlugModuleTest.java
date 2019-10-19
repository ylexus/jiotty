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

import static net.yudichev.jiotty.common.inject.SpecifiedAnnotation.forAnnotation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TpLinkSmartPlugModuleTest {
    @Test
    void testCreateInjector() {
        Named annotation = Names.named("annotation");
        ExposedKeyModule<Appliance> module = TpLinkSmartPlugModule.builder()
                .setDeviceId("did")
                .setTermId("tid")
                .setUsername("u")
                .setPassword("p")
                .withAnnotation(forAnnotation(annotation))
                .build();

        assertThat(module.getExposedKey(), is(Key.get(Appliance.class, annotation)));

        Injector injector = Guice.createInjector(module,
                new ExecutorModule());

        injector.getBinding(module.getExposedKey());
    }
}