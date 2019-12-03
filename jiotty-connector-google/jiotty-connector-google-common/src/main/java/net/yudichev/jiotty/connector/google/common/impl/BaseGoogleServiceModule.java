package net.yudichev.jiotty.connector.google.common.impl;

import com.google.common.reflect.TypeToken;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;
import net.yudichev.jiotty.connector.google.common.AuthorizationBrowser;
import net.yudichev.jiotty.connector.google.common.GoogleApiAuthSettings;
import net.yudichev.jiotty.connector.google.common.ResolvedGoogleApiAuthSettings;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.lang.Optionals.ifPresent;
import static net.yudichev.jiotty.connector.google.common.impl.Bindings.Settings;

public abstract class BaseGoogleServiceModule extends BaseLifecycleComponentModule {
    private final GoogleApiAuthSettings settings;

    protected BaseGoogleServiceModule(GoogleApiAuthSettings settings) {
        this.settings = checkNotNull(settings);
    }

    @Override
    protected final void configure() {
        ifPresent(settings.authorizationBrowser(), authorizationBrowserBindingSpec -> authorizationBrowserBindingSpec
                .map(TypeToken.of(AuthorizationBrowser.class), new TypeToken<Optional<AuthorizationBrowser>>() {}, Optional::of)
                .bind(new TypeLiteral<Optional<AuthorizationBrowser>>() {})
                .annotatedWith(GoogleApiAuthSettingsResolver.Dependency.class)
                .installedBy(this::installLifecycleComponentModule))
                .orElse(() -> bind(new TypeLiteral<Optional<AuthorizationBrowser>>() {})
                        .annotatedWith(GoogleApiAuthSettingsResolver.Dependency.class)
                        .toInstance(Optional.empty()));
        bind(GoogleApiAuthSettings.class).annotatedWith(GoogleApiAuthSettingsResolver.Dependency.class).toInstance(settings);
        bind(ResolvedGoogleApiAuthSettings.class).annotatedWith(Settings.class).toProvider(GoogleApiAuthSettingsResolver.class);

        doConfigure();
    }

    protected abstract void doConfigure();

    public abstract static class BaseBuilder<T extends Module, B extends BaseBuilder<T, B>> implements TypedBuilder<T> {
        private GoogleApiAuthSettings settings;

        protected GoogleApiAuthSettings getSettings() {
            return settings;
        }

        public B setSettings(GoogleApiAuthSettings settings) {
            this.settings = settings;
            return thisBuilder();
        }

        protected abstract B thisBuilder();
    }
}
