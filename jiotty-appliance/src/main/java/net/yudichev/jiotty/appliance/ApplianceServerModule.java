package net.yudichev.jiotty.appliance;

import com.google.inject.Module;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.lang.TypedBuilder;
import net.yudichev.jiotty.common.rest.RestServer;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.boundTo;

public final class ApplianceServerModule extends BaseLifecycleComponentModule {
    private final BindingSpec<String> applianceIdSpec;
    private final BindingSpec<Appliance> applianceSpec;
    private final BindingSpec<RestServer> restServerSpec;

    private ApplianceServerModule(BindingSpec<String> applianceIdSpec,
                                  BindingSpec<Appliance> applianceSpec,
                                  BindingSpec<RestServer> restServerSpec) {
        this.applianceIdSpec = checkNotNull(applianceIdSpec);
        this.applianceSpec = checkNotNull(applianceSpec);
        this.restServerSpec = checkNotNull(restServerSpec);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        applianceIdSpec.bind(String.class)
                .annotatedWith(ApplianceServer.ApplianceId.class)
                .installedBy(this::installLifecycleComponentModule);
        applianceSpec.bind(Appliance.class)
                .annotatedWith(ApplianceServer.Dependency.class)
                .installedBy(this::installLifecycleComponentModule);
        restServerSpec.bind(RestServer.class)
                .annotatedWith(ApplianceServer.Dependency.class)
                .installedBy(this::installLifecycleComponentModule);
        registerLifecycleComponent(ApplianceServer.class);
    }

    public static final class Builder implements TypedBuilder<Module> {
        private BindingSpec<String> applianceIdSpec;
        private BindingSpec<Appliance> applianceSpec;
        private BindingSpec<RestServer> restServerSpec = boundTo(RestServer.class);

        public Builder setApplianceId(BindingSpec<String> applianceIdSpec) {
            this.applianceIdSpec = checkNotNull(applianceIdSpec);
            return this;
        }

        public Builder setAppliance(BindingSpec<Appliance> applianceSpec) {
            this.applianceSpec = checkNotNull(applianceSpec);
            return this;
        }

        public Builder withRestServer(BindingSpec<RestServer> restServerSpec) {
            this.restServerSpec = checkNotNull(restServerSpec);
            return this;
        }

        @Override
        public Module build() {
            return new ApplianceServerModule(applianceIdSpec, applianceSpec, restServerSpec);
        }
    }
}
