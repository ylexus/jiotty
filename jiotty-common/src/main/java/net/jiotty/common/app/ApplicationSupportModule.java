package net.jiotty.common.app;

import com.google.inject.AbstractModule;

import static com.google.common.base.Preconditions.checkNotNull;

final class ApplicationSupportModule extends AbstractModule {
    private final ApplicationLifecycleControl applicationLifecycleControl;

    ApplicationSupportModule(ApplicationLifecycleControl applicationLifecycleControl) {
        this.applicationLifecycleControl = checkNotNull(applicationLifecycleControl);
    }

    @Override
    protected void configure() {
        bind(ApplicationLifecycleControl.class).toInstance(applicationLifecycleControl);
    }
}
