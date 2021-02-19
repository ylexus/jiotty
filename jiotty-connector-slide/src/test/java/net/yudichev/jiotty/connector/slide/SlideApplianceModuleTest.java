package net.yudichev.jiotty.connector.slide;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import net.yudichev.jiotty.appliance.Appliance;
import net.yudichev.jiotty.common.async.ExecutorFactory;
import net.yudichev.jiotty.common.async.ExecutorModule;
import net.yudichev.jiotty.common.async.backoff.BackOffConfig;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.time.TimeModule;
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
                .withRetries(literally(BackOffConfig.builder().build()))
                .build();
        Injector injector = Guice.createInjector(
                new TimeModule(),
                new ExecutorModule(),
                SlideServiceModule.builder()
                        .setEmail(literally("email"))
                        .setPassword(literally("password"))
                        .withPositionVerification()
                        .build(),
                applianceModule);

        assertThat(applianceModule.getExposedKey(), is(Key.get(Appliance.class)));
        injector.getBinding(applianceModule.getExposedKey());
    }
}