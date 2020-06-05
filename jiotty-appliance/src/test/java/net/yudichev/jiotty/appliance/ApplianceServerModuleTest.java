package net.yudichev.jiotty.appliance;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import net.yudichev.jiotty.common.inject.LifecycleComponent;
import net.yudichev.jiotty.common.rest.RestServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.yudichev.jiotty.common.inject.BindingSpec.literally;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(MockitoExtension.class)
class ApplianceServerModuleTest {
    @Mock
    private Appliance appliance;
    @Mock
    private RestServer restServer;

    @Test
    void bindings() {
        Injector injector = Guice.createInjector(ApplianceServerModule.builder()
                .setApplianceId(literally("applianceId"))
                .setAppliance(literally(appliance))
                .withRestServer(literally(restServer))
                .build());
        assertThat(injector.findBindingsByType(new TypeLiteral<LifecycleComponent>() {}), is(not(empty())));
    }
}