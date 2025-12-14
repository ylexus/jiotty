package net.yudichev.jiotty.user.ui;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;

public final class UIServerModule extends BaseLifecycleComponentModule implements ExposedKeyModule<UIServer> {
    public static final int LISTEN_PORT = 4568;
    public static final String PATH_ROOT = "/ui";
    public static final String SUB_PATH_OPTIONS = "/options";

    @Override
    protected void configure() {
        bind(OptionPersistence.class).to(OptionPersistenceImpl.class);
        bind(getExposedKey()).to(registerLifecycleComponent(UIServerImpl.class));
        expose(getExposedKey());
    }
}
