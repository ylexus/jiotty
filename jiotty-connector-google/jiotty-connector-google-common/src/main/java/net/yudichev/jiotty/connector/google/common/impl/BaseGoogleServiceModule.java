package net.yudichev.jiotty.connector.google.common.impl;

import com.google.inject.Module;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;
import net.yudichev.jiotty.connector.google.common.GoogleApiSettings;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.connector.google.common.impl.Bindings.Settings;

public abstract class BaseGoogleServiceModule extends BaseLifecycleComponentModule {
    private final GoogleApiSettings settings;

    protected BaseGoogleServiceModule(GoogleApiSettings settings) {
        this.settings = checkNotNull(settings);
    }

    @Override
    protected final void configure() {
        bind(GoogleApiSettings.class).annotatedWith(Settings.class).toInstance(settings);

        doConfigure();
    }

    protected abstract void doConfigure();

    public abstract static class BaseBuilder<T extends Module, B extends BaseBuilder<T, B>> implements TypedBuilder<T> {
        private GoogleApiSettings settings;

        protected GoogleApiSettings getSettings() {
            return settings;
        }

        public B setSettings(GoogleApiSettings settings) {
            this.settings = settings;
            return thisBuilder();
        }

        protected abstract B thisBuilder();
    }
}