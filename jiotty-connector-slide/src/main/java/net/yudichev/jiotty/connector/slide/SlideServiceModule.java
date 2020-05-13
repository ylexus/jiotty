package net.yudichev.jiotty.connector.slide;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SlideServiceModule extends BaseLifecycleComponentModule implements ExposedKeyModule<SlideService> {
    private final BindingSpec<String> emailSpec;
    private final BindingSpec<String> passwordSpec;

    private SlideServiceModule(BindingSpec<String> emailSpec, BindingSpec<String> passwordSpec) {
        this.emailSpec = checkNotNull(emailSpec);
        this.passwordSpec = checkNotNull(passwordSpec);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        emailSpec.bind(String.class)
                .annotatedWith(Bindings.Email.class)
                .installedBy(this::installLifecycleComponentModule);
        passwordSpec.bind(String.class)
                .annotatedWith(Bindings.Password.class)
                .installedBy(this::installLifecycleComponentModule);

        bind(getExposedKey()).to(boundLifecycleComponent(SlideServiceImpl.class));
        expose(getExposedKey());
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<SlideService>> {
        private BindingSpec<String> emailSpec;
        private BindingSpec<String> passwordSpec;

        public Builder setEmail(BindingSpec<String> emailSpec) {
            this.emailSpec = emailSpec;
            return this;
        }

        public Builder setPassword(BindingSpec<String> passwordSpec) {
            this.passwordSpec = passwordSpec;
            return this;
        }

        @Override
        public ExposedKeyModule<SlideService> build() {
            return new SlideServiceModule(emailSpec, passwordSpec);
        }
    }
}
