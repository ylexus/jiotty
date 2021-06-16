package net.yudichev.jiotty.connector.google.sdm;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.smartdevicemanagement.v1.SmartDeviceManagement;
import net.yudichev.jiotty.connector.google.common.GoogleAuthorization;
import net.yudichev.jiotty.connector.google.common.impl.Bindings.Authorization;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;

final class SmartDeviceManagementProvider implements Provider<SmartDeviceManagement> {

    private final Provider<GoogleAuthorization> googleAuthorizationProvider;

    @Inject
    SmartDeviceManagementProvider(@Authorization Provider<GoogleAuthorization> googleAuthorizationProvider) {
        this.googleAuthorizationProvider = checkNotNull(googleAuthorizationProvider);
    }

    @Override
    public SmartDeviceManagement get() {
        return getAsUnchecked(() -> {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            return new SmartDeviceManagement.Builder(httpTransport, JacksonFactory.getDefaultInstance(), googleAuthorizationProvider.get().getCredential())
                    .setApplicationName("jiotty")
                    .build();
        });
    }
}
