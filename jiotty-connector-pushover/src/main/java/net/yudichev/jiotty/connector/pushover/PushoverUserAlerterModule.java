package net.yudichev.jiotty.connector.pushover;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;

import static com.google.common.base.Preconditions.checkNotNull;

public final class PushoverUserAlerterModule extends BaseLifecycleComponentModule implements ExposedKeyModule<UserAlerter> {
    private final BindingSpec<String> apiTokenSpec;

    public PushoverUserAlerterModule(BindingSpec<String> apiTokenSpec) {
        this.apiTokenSpec = checkNotNull(apiTokenSpec);
    }

    @Override
    protected void configure() {
        apiTokenSpec.bind(String.class).annotatedWith(PushoverUserAlerter.ApiToken.class).installedBy(this::installLifecycleComponentModule);
        bind(getExposedKey()).to(registerLifecycleComponent(PushoverUserAlerter.class));
        expose(getExposedKey());
    }
}
