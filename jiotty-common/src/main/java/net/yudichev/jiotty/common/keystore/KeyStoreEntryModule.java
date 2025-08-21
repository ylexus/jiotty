package net.yudichev.jiotty.common.keystore;

import com.google.inject.Key;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.inject.SpecifiedAnnotation;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.GuiceUtil.uniqueAnnotation;

public final class KeyStoreEntryModule extends BaseLifecycleComponentModule implements ExposedKeyModule<String> {
    private final BindingSpec<String> aliasSpec;
    private final Key<String> key;

    public KeyStoreEntryModule(BindingSpec<String> aliasSpec) {
        this(aliasSpec, SpecifiedAnnotation.forAnnotation(uniqueAnnotation()));
    }

    public KeyStoreEntryModule(BindingSpec<String> aliasSpec, SpecifiedAnnotation annotation) {
        this.aliasSpec = checkNotNull(aliasSpec);
        key = annotation.specify(ExposedKeyModule.super.getExposedKey().getTypeLiteral());
    }

    public static BindingSpec<String> keyStoreEntry(String alias) {
        return BindingSpec.exposedBy(new KeyStoreEntryModule(BindingSpec.literally(alias)));
    }

    @Override
    public Key<String> getExposedKey() {
        return key;
    }

    @Override
    protected void configure() {
        aliasSpec.bind(String.class).annotatedWith(KeyStoreEntryProvider.Alias.class).installedBy(this::installLifecycleComponentModule);
        bind(key).toProvider(KeyStoreEntryProvider.class);
        expose(key);
    }
}
