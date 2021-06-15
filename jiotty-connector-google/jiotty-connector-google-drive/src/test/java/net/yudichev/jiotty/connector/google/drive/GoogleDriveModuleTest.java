package net.yudichev.jiotty.connector.google.drive;

import com.google.inject.Guice;
import net.yudichev.jiotty.connector.google.common.GoogleAuthorization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.yudichev.jiotty.common.inject.BindingSpec.literally;

@ExtendWith(MockitoExtension.class)
class GoogleDriveModuleTest {
    @Test
    void configure(@Mock GoogleAuthorization authorization) {
        var module = GoogleDriveModule.builder()
                .withAuthorization(literally(authorization))
                .build();
        Guice.createInjector(module).getBinding(module.getExposedKey());
    }
}