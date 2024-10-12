package net.yudichev.jiotty.connector.octopusenergy;

import com.google.inject.Guice;
import net.yudichev.jiotty.common.time.TimeModule;
import org.junit.jupiter.api.Test;

import static net.yudichev.jiotty.common.inject.BindingSpec.literally;

class OctopusEnergyModuleTest {
    @Test
    void configure() {
        var module = OctopusEnergyModule.builder()
                                        .setApiKey(literally("apiKey"))
                                        .setAccountId(literally("accountId"))
                                        .build();
        Guice.createInjector(new TimeModule(), module).getBinding(module.getExposedKey());
    }
}