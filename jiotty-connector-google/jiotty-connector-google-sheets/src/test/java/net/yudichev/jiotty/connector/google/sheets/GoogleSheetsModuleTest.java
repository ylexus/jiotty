package net.yudichev.jiotty.connector.google.sheets;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.connector.google.common.GoogleAuthorization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static net.yudichev.jiotty.common.inject.BindingSpec.literally;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class GoogleSheetsModuleTest {
    @Test
    void createInjector(@Mock GoogleAuthorization googleAuthorization) {
        ExposedKeyModule<GoogleSheetsClient> module = GoogleSheetsModule.builder()
                .withAuthorization(literally(googleAuthorization))
                .build();
        assertThat(module.getExposedKey(), is(Key.get(TypeLiteral.get(GoogleSheetsClient.class))));

        Guice.createInjector(module).getBinding(module.getExposedKey());
    }
}