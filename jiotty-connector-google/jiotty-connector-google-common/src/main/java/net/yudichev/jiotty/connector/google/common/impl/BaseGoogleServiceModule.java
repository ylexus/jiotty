package net.yudichev.jiotty.connector.google.common.impl;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;
import net.yudichev.jiotty.connector.google.common.GoogleAuthorization;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.boundTo;
import static net.yudichev.jiotty.connector.google.common.impl.Bindings.Authorization;

public abstract class BaseGoogleServiceModule extends BaseLifecycleComponentModule {

    private final BindingSpec<GoogleAuthorization> googleAuthorizationSpec;

    protected BaseGoogleServiceModule(BindingSpec<GoogleAuthorization> googleAuthorizationSpec) {
        this.googleAuthorizationSpec = checkNotNull(googleAuthorizationSpec);
    }

    @Override
    protected final void configure() {
        googleAuthorizationSpec.bind(GoogleAuthorization.class)
                .annotatedWith(Authorization.class)
                .installedBy(this::installLifecycleComponentModule);
        doConfigure();
    }

    protected abstract void doConfigure();

    public abstract static class BaseBuilder<T, B extends BaseBuilder<T, B>> implements TypedBuilder<ExposedKeyModule<T>> {
        private BindingSpec<GoogleAuthorization> authorizationSpec = boundTo(GoogleAuthorization.class);

        protected BindingSpec<GoogleAuthorization> getAuthorizationSpec() {
            return authorizationSpec;
        }

        public B withAuthorization(BindingSpec<GoogleAuthorization> authorizationSpec) {
            this.authorizationSpec = checkNotNull(authorizationSpec);
            return thisBuilder();
        }

        protected abstract B thisBuilder();
    }
}
