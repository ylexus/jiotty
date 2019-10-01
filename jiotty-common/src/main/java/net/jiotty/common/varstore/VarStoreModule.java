package net.jiotty.common.varstore;

import net.jiotty.common.inject.BaseLifecycleComponentModule;
import net.jiotty.common.inject.ExposedKeyModule;

import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VarStoreModule extends BaseLifecycleComponentModule implements ExposedKeyModule<VarStore> {
    private final String applicationName;

    public VarStoreModule(String applicationName) {
        this.applicationName = checkNotNull(applicationName);
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(VarStoreImpl.AppName.class).to(applicationName);
        bind(getExposedKey()).to(VarStoreImpl.class).in(Singleton.class);
        expose(getExposedKey());
    }
}
