package net.yudichev.jiotty.appliance;

import com.google.inject.Key;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;

import java.lang.annotation.Annotation;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ApplianceServerModule extends BaseLifecycleComponentModule {
    private final String applianceId;
    private final Class<? extends Annotation> bindingAnnotation;

    public ApplianceServerModule(String applianceId, Class<? extends Annotation> bindingAnnotation) {
        this.applianceId = checkNotNull(applianceId);
        this.bindingAnnotation = checkNotNull(bindingAnnotation);
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(ApplianceServer.ApplianceId.class).to(applianceId);
        bind(Appliance.class).annotatedWith(ApplianceServer.Dependency.class).to(Key.get(Appliance.class, bindingAnnotation));
        boundLifecycleComponent(ApplianceServer.class);
    }
}
