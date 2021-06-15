package net.yudichev.jiotty.connector.google.common;

import com.google.inject.Guice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import static net.yudichev.jiotty.common.inject.BindingSpec.literally;

@ExtendWith(MockitoExtension.class)
class GoogleAuthorizationModuleTest {
    @Test
    void configure(@Mock AuthorizationBrowser authorizationBrowser) throws MalformedURLException {
        var module = GoogleAuthorizationModule.builder()
                .setSettings(GoogleApiAuthSettings.builder()
                        .setApplicationName("appName")
                        .setAuthDataStoreRootDir(Paths.get("."))
                        .setCredentialsUrl(literally(new URL("file:///.")))
                        .setAuthorizationBrowser(literally(authorizationBrowser))
                        .build())
                .build();
        Guice.createInjector(module).getBinding(module.getExposedKey());
    }
}