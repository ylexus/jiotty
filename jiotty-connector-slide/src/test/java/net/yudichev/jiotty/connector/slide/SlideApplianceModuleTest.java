package net.yudichev.jiotty.connector.slide;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import net.yudichev.jiotty.appliance.Appliance;
import net.yudichev.jiotty.common.async.ExecutorFactory;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.yudichev.jiotty.common.inject.BindingSpec.literally;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class SlideApplianceModuleTest {
    @Mock
    private ExecutorFactory executorFactory;

    @Test
    void test() {
        ExposedKeyModule<Appliance> applianceModule = SlideApplianceModule.builder()
                .setSlideIdSpec(literally(1L))
                .build();
        Injector injector = Guice.createInjector(
                binder -> binder.bind(ExecutorFactory.class).toInstance(executorFactory),
                SlideServiceModule.builder()
                        .setEmail(literally("email"))
                        .setPassword(literally("password"))
                        .build(),
                applianceModule);

        assertThat(applianceModule.getExposedKey(), is(Key.get(Appliance.class)));
        injector.getBinding(applianceModule.getExposedKey());
    }
}