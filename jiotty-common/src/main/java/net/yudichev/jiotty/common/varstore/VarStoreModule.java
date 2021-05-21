package net.yudichev.jiotty.common.varstore;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import javax.inject.Singleton;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VarStoreModule extends BaseLifecycleComponentModule implements ExposedKeyModule<VarStore> {
    private final BindingSpec<Path> pathSpec;

    public VarStoreModule(BindingSpec<Path> pathSpec) {
        this.pathSpec = checkNotNull(pathSpec);
    }

    @Override
    protected void configure() {
        pathSpec.bind(Path.class)
                .annotatedWith(VarStoreImpl.StoreFile.class)
                .installedBy(this::installLifecycleComponentModule);
        bind(getExposedKey()).to(VarStoreImpl.class).in(Singleton.class);
        expose(getExposedKey());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<VarStore>> {

        private BindingSpec<Path> pathSpec;

        private Builder() {
        }

        public Builder setPath(BindingSpec<Path> pathSpec) {
            this.pathSpec = checkNotNull(pathSpec);
            return this;
        }

        @Override
        public ExposedKeyModule<VarStore> build() {
            return new VarStoreModule(pathSpec);
        }
    }
}
