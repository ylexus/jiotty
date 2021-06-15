package net.yudichev.jiotty.connector.google.sdm;

import com.google.inject.Guice;
import net.yudichev.jiotty.connector.google.common.GoogleAuthorization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.yudichev.jiotty.common.inject.BindingSpec.literally;

@ExtendWith(MockitoExtension.class)
class GoogleSmartDeviceManagementClientModuleTest {
    @Test
    void configure(@Mock GoogleAuthorization authorization) {
        var module = GoogleSmartDeviceManagementClientModule.builder()
                .withAuthorization(literally(authorization))
                .setProjectId(literally("projectId"))
                .build();
        Guice.createInjector(module).getBinding(module.getExposedKey());
    }
}