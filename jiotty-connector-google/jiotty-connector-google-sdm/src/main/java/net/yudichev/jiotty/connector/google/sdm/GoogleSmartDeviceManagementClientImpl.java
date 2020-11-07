package net.yudichev.jiotty.connector.google.sdm;

import com.google.api.services.smartdevicemanagement.v1.SmartDeviceManagement;
import com.google.inject.BindingAnnotation;

import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

final class GoogleSmartDeviceManagementClientImpl implements GoogleSmartDeviceManagementClient {
    private final SmartDeviceManagement smartDeviceManagement;
    private final String projectId;

    @Inject
    GoogleSmartDeviceManagementClientImpl(@Dependency SmartDeviceManagement smartDeviceManagement,
                                          @ProjectId String projectId) {
        this.smartDeviceManagement = checkNotNull(smartDeviceManagement);
        this.projectId = checkNotNull(projectId);
    }

    @Override
    public GoogleNestThermostat getThermostat(String deviceId, Executor executor) {
        return new InternalGoogleNestThermostat(smartDeviceManagement, projectId, deviceId, executor);
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface ProjectId {
    }

}
