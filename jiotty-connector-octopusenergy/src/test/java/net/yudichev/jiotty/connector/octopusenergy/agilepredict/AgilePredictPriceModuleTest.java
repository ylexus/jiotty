package net.yudichev.jiotty.connector.octopusenergy.agilepredict;

import com.google.inject.Guice;
import net.yudichev.jiotty.common.time.TimeModule;
import org.junit.jupiter.api.Test;

class AgilePredictPriceModuleTest {
    @Test
    void configure() {
        var module = new AgilePredictPriceModule();
        Guice.createInjector(new TimeModule(), module).getBinding(module.getExposedKey());
    }
}