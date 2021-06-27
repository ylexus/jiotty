package net.yudichev.jiotty.connector.slide;

import com.google.inject.Key;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.inject.*;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import javax.inject.Singleton;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.literally;
import static net.yudichev.jiotty.common.inject.SpecifiedAnnotation.forNoAnnotation;
import static net.yudichev.jiotty.connector.slide.Bindings.*;

public final class SlideServiceModule extends BaseLifecycleComponentModule implements ExposedKeyModule<SlideService> {
    private final BindingSpec<String> emailSpec;
    private final BindingSpec<String> passwordSpec;
    private final Optional<BindingSpec<Double>> positionVerificationToleranceSpec;
    private final Key<SlideService> exposedKey;

    private SlideServiceModule(BindingSpec<String> emailSpec,
                               BindingSpec<String> passwordSpec,
                               Optional<BindingSpec<Double>> positionVerificationToleranceSpec,
                               SpecifiedAnnotation specifiedAnnotation) {
        this.emailSpec = checkNotNull(emailSpec);
        this.passwordSpec = checkNotNull(passwordSpec);
        this.positionVerificationToleranceSpec = checkNotNull(positionVerificationToleranceSpec);
        exposedKey = specifiedAnnotation.specify(ExposedKeyModule.super.getExposedKey().getTypeLiteral());
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Key<SlideService> getExposedKey() {
        return exposedKey;
    }

    @Override
    protected void configure() {
        emailSpec.bind(String.class)
                .annotatedWith(Email.class)
                .installedBy(this::installLifecycleComponentModule);
        passwordSpec.bind(String.class)
                .annotatedWith(Password.class)
                .installedBy(this::installLifecycleComponentModule);

        bind(SchedulingExecutor.class).annotatedWith(ServiceExecutor.class).toProvider(boundLifecycleComponent(ExecutorProvider.class)).in(Singleton.class);
        positionVerificationToleranceSpec.ifPresentOrElse(
                toleranceSpec -> {
                    toleranceSpec.bind(Double.class)
                            .annotatedWith(VerifyingSlideService.Tolerance.class)
                            .installedBy(this::installLifecycleComponentModule);
                    bind(SlideService.class).annotatedWith(VerifyingSlideService.Delegate.class).to(boundLifecycleComponent(SlideServiceImpl.class));
                    bind(exposedKey).to(VerifyingSlideService.class);
                },
                () -> bind(exposedKey).to(boundLifecycleComponent(SlideServiceImpl.class)));
        expose(exposedKey);
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<SlideService>>, HasWithAnnotation {
        private BindingSpec<String> emailSpec;
        private BindingSpec<String> passwordSpec;
        private SpecifiedAnnotation specifiedAnnotation = forNoAnnotation();
        private BindingSpec<Double> positionVerificationToleranceSpec;

        public Builder setEmail(BindingSpec<String> emailSpec) {
            this.emailSpec = checkNotNull(emailSpec);
            return this;
        }

        public Builder setPassword(BindingSpec<String> passwordSpec) {
            this.passwordSpec = checkNotNull(passwordSpec);
            return this;
        }

        public Builder withPositionVerification() {
            return withPositionVerification(literally(0.11));
        }

        public Builder withPositionVerification(BindingSpec<Double> positionVerificationToleranceSpec) {
            this.positionVerificationToleranceSpec = checkNotNull(positionVerificationToleranceSpec);
            return this;
        }

        @Override
        public Builder withAnnotation(SpecifiedAnnotation specifiedAnnotation) {
            this.specifiedAnnotation = checkNotNull(specifiedAnnotation);
            return this;
        }

        @Override
        public ExposedKeyModule<SlideService> build() {
            return new SlideServiceModule(emailSpec, passwordSpec, Optional.ofNullable(positionVerificationToleranceSpec), specifiedAnnotation);
        }
    }
}
