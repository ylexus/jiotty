package net.yudichev.jiotty.connector.slide;

import com.google.inject.Key;
import net.yudichev.jiotty.common.inject.*;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SlideServiceModule extends BaseLifecycleComponentModule implements ExposedKeyModule<SlideService> {
    private final BindingSpec<String> emailSpec;
    private final BindingSpec<String> passwordSpec;
    private final Key<SlideService> exposedKey;

    private SlideServiceModule(BindingSpec<String> emailSpec,
                               BindingSpec<String> passwordSpec,
                               SpecifiedAnnotation specifiedAnnotation) {
        this.emailSpec = checkNotNull(emailSpec);
        this.passwordSpec = checkNotNull(passwordSpec);
        exposedKey = specifiedAnnotation.specify(ExposedKeyModule.super.getExposedKey().getTypeLiteral());
    }

    @Override
    public Key<SlideService> getExposedKey() {
        return exposedKey;
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

        bind(exposedKey).to(boundLifecycleComponent(SlideServiceImpl.class));
        expose(exposedKey);
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<SlideService>>, HasWithAnnotation {
        private BindingSpec<String> emailSpec;
        private BindingSpec<String> passwordSpec;
        private SpecifiedAnnotation specifiedAnnotation;

        public Builder setEmail(BindingSpec<String> emailSpec) {
            this.emailSpec = emailSpec;
            return this;
        }

        public Builder setPassword(BindingSpec<String> passwordSpec) {
            this.passwordSpec = passwordSpec;
            return this;
        }

        @Override
        public Builder withAnnotation(SpecifiedAnnotation specifiedAnnotation) {
            this.specifiedAnnotation = checkNotNull(specifiedAnnotation);
            return this;
        }

        @Override
        public ExposedKeyModule<SlideService> build() {
            return new SlideServiceModule(emailSpec, passwordSpec, specifiedAnnotation);
        }
    }
}
