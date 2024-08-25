package net.yudichev.jiotty.logging;

import com.google.inject.Guice;
import net.yudichev.jiotty.common.varstore.VarStoreModule;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static net.yudichev.jiotty.common.inject.BindingSpec.literally;

class PersistingLog4jLevelConfiguratorModuleTest {
    @Test
    void configure() {
        var module = PersistingLog4jLevelConfiguratorModule.builder().build();
        Guice.createInjector(new VarStoreModule(literally(Paths.get(System.getProperty("java.io.tmpdir")))), module)
             .getBinding(module.getExposedKey());

    }
}