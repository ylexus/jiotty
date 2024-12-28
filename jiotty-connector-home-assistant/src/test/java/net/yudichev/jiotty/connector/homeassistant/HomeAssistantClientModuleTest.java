package net.yudichev.jiotty.connector.homeassistant;

import com.google.inject.Guice;
import org.junit.jupiter.api.Test;

import static net.yudichev.jiotty.common.inject.BindingSpec.literally;

class HomeAssistantClientModuleTest {
    @Test
    void configure() {
        var module = HomeAssistantClientModule.builder()
                                              .setBaseUrlSpec(literally("baseUrl"))
                                              .setAccessTokenSpec(literally("accessToken"))
                                              .build();
        Guice.createInjector(module).getInstance(module.getExposedKey());
    }
}