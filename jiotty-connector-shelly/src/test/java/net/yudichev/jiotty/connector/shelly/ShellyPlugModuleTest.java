package net.yudichev.jiotty.connector.shelly;

import com.google.inject.Guice;
import net.yudichev.jiotty.common.async.ExecutorModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.time.TimeModule;
import org.junit.jupiter.api.Test;

class ShellyPlugModuleTest {
    @Test
    void configures() {
        var module = ShellyPlugModule.builder()
                                     .setHost(BindingSpec.literally("host"))
                                     .build();
        Guice.createInjector(new TimeModule(), new ExecutorModule(), module)
             .getBinding(module.getExposedKey());
    }
}