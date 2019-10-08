package net.yudichev.jiotty.common.inject;

import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class BaseLifecycleComponentModule extends PrivateModule {
    private final Set<Key<? extends LifecycleComponent>> lifecycleComponentKeys = new LinkedHashSet<>();

    protected final void installLifecycleComponentModule(Module module) {
        if (module instanceof BaseLifecycleComponentModule) {
            install(module);
            ((BaseLifecycleComponentModule) module).lifecycleComponentKeys.forEach(this::exposeKey);
        } else {
            install(module);
        }
    }

    protected final <T extends LifecycleComponent> Key<T> boundLifecycleComponent(Class<T> implClass) {
        Key<T> implKey = Key.get(implClass);
        bind(implKey).in(Singleton.class);
        Key<LifecycleComponent> lifecycleComponentKey = Key.get(LifecycleComponent.class, GuiceUtil.uniqueAnnotation());
        bind(lifecycleComponentKey).to(implKey);
        exposeKey(lifecycleComponentKey);
        return implKey;
    }

    private void exposeKey(Key<? extends LifecycleComponent> key) {
        expose(key);
        lifecycleComponentKeys.add(key);
    }
}
