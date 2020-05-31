package net.yudichev.jiotty.connector.slide;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import net.yudichev.jiotty.appliance.Appliance;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import org.junit.jupiter.api.Test;

import static net.yudichev.jiotty.common.inject.BindingSpec.literally;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SlideApplianceModuleTest {
    @Test
    void test() {
        ExposedKeyModule<Appliance> applianceModule = SlideApplianceModule.builder()
                .setSlideIdSpec(literally(1L))
                .build();
        Injector injector = Guice.createInjector(SlideServiceModule.builder()
                        .setEmail(literally("email"))
                        .setPassword(literally("password"))
                        .build(),
                applianceModule);

        assertThat(applianceModule.getExposedKey(), is(Key.get(Appliance.class)));
        injector.getBinding(applianceModule.getExposedKey());
    }
}