package net.yudichev.jiotty.connector.google.sdm;

import com.google.api.services.smartdevicemanagement.v1.SmartDeviceManagement;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.connector.google.common.GoogleApiAuthSettings;
import net.yudichev.jiotty.connector.google.common.impl.BaseGoogleServiceModule;

import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

public final class GoogleSmartDeviceManagementClientModule extends BaseGoogleServiceModule implements ExposedKeyModule<GoogleSmartDeviceManagementClient> {
    private final BindingSpec<String> projectId;

    private GoogleSmartDeviceManagementClientModule(GoogleApiAuthSettings settings, BindingSpec<String> projectId) {
        super(settings);
        this.projectId = checkNotNull(projectId, "projectId is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void doConfigure() {
        projectId.bind(String.class)
                .annotatedWith(GoogleSmartDeviceManagementClientImpl.ProjectId.class)
                .installedBy(this::installLifecycleComponentModule);
        bind(SmartDeviceManagement.class).annotatedWith(GoogleSmartDeviceManagementClientImpl.Dependency.class)
                .toProvider(SmartDeviceManagementProvider.class).in(Singleton.class);
        bind(getExposedKey()).to(GoogleSmartDeviceManagementClientImpl.class);
        expose(getExposedKey());
    }

    public static final class Builder extends BaseBuilder<ExposedKeyModule<GoogleSmartDeviceManagementClient>, Builder> {
        private BindingSpec<String> projectId;

        public Builder setProjectId(BindingSpec<String> projectId) {
            this.projectId = checkNotNull(projectId);
            return this;
        }

        @Override
        public ExposedKeyModule<GoogleSmartDeviceManagementClient> build() {
            return new GoogleSmartDeviceManagementClientModule(getSettings(), projectId);
        }

        @Override
        protected Builder thisBuilder() {
            return this;
        }
    }
}
