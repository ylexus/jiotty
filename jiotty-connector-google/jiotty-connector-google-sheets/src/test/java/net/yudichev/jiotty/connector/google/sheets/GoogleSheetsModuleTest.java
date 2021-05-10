package net.yudichev.jiotty.connector.google.sheets;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.connector.google.common.AuthorizationBrowser;
import net.yudichev.jiotty.connector.google.common.GoogleApiAuthSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import static net.yudichev.jiotty.common.inject.BindingSpec.literally;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class GoogleSheetsModuleTest {
    @Mock
    private AuthorizationBrowser authBrowser;

    @Test
    void createInjector() throws MalformedURLException {
        ExposedKeyModule<GoogleSheetsClient> module = GoogleSheetsModule.builder()
                .setSettings(GoogleApiAuthSettings.builder()
                        .setAuthorizationBrowser(literally(authBrowser))
                        .setApplicationName("an")
                        .setCredentialsUrl(literally(new URL("file:/url")))
                        .setAuthDataStoreRootDir(Paths.get("adsrd"))
                        .build())
                .build();
        assertThat(module.getExposedKey(), is(Key.get(TypeLiteral.get(GoogleSheetsClient.class))));

        Injector injector = Guice.createInjector(module);

        injector.getBinding(module.getExposedKey());
    }
}