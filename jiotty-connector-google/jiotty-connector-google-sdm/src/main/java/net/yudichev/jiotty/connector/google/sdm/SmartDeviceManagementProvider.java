package net.yudichev.jiotty.connector.google.sdm;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.smartdevicemanagement.v1.SmartDeviceManagement;
import com.google.api.services.smartdevicemanagement.v1.SmartDeviceManagementScopes;
import net.yudichev.jiotty.connector.google.common.ResolvedGoogleApiAuthSettings;
import net.yudichev.jiotty.connector.google.common.impl.GoogleAuthorization;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.yudichev.jiotty.connector.google.common.impl.Bindings.Settings;

final class SmartDeviceManagementProvider implements Provider<SmartDeviceManagement> {
    private final ResolvedGoogleApiAuthSettings settings;

    @Inject
    SmartDeviceManagementProvider(@Settings ResolvedGoogleApiAuthSettings settings) {
        this.settings = checkNotNull(settings);
    }

    @Override
    public SmartDeviceManagement get() {
        return getAsUnchecked(() -> {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            return new SmartDeviceManagement.Builder(
                    httpTransport,
                    JacksonFactory.getDefaultInstance(),
                    GoogleAuthorization.builder()
                            .setHttpTransport(httpTransport)
                            .setAuthDataStoreRootDir(settings.authDataStoreRootDir())
                            .setApiName("sdm")
                            .setCredentialsUrl(settings.credentialsUrl())
                            .addRequiredScope(SmartDeviceManagementScopes.SDM_SERVICE)
                            .withBrowser(settings.authorizationBrowser())
                            .build()
                            .getCredential())
                    .setApplicationName(settings.applicationName())
                    .build();
        });
    }
}
