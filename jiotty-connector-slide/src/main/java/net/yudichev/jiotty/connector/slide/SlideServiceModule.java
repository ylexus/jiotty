package net.yudichev.jiotty.connector.slide;

import com.google.inject.Key;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.inject.HasWithAnnotation;
import net.yudichev.jiotty.common.inject.SpecifiedAnnotation;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static net.yudichev.jiotty.common.inject.BindingSpec.literally;
import static net.yudichev.jiotty.common.inject.SpecifiedAnnotation.forNoAnnotation;
import static net.yudichev.jiotty.connector.slide.Bindings.DeviceCode;
import static net.yudichev.jiotty.connector.slide.Bindings.DeviceHost;
import static net.yudichev.jiotty.connector.slide.Bindings.Email;
import static net.yudichev.jiotty.connector.slide.Bindings.Password;
import static net.yudichev.jiotty.connector.slide.Bindings.ServiceExecutor;

@SuppressWarnings("OverlyCoupledClass") // module
public final class SlideServiceModule extends BaseLifecycleComponentModule implements ExposedKeyModule<SlideService> {
    private final BindingSpec<String> hostSpec;
    private final BindingSpec<String> deviceCodeSpec;
    private final BindingSpec<String> emailSpec;
    private final BindingSpec<String> passwordSpec;
    private final Optional<BindingSpec<Double>> positionVerificationToleranceSpec;
    private final Key<SlideService> exposedKey;

    private boolean executorBound;

    private SlideServiceModule(BindingSpec<String> hostSpec,
                               BindingSpec<String> deviceCodeSpec,
                               BindingSpec<String> emailSpec,
                               BindingSpec<String> passwordSpec,
                               Optional<BindingSpec<Double>> positionVerificationToleranceSpec,
                               SpecifiedAnnotation specifiedAnnotation) {
        this.hostSpec = hostSpec;
        this.deviceCodeSpec = deviceCodeSpec;
        this.emailSpec = emailSpec;
        this.passwordSpec = passwordSpec;
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
        Supplier<Key<? extends SlideService>> serivceImplKeySupplier;

        if (hostSpec != null) {
            hostSpec.bind(String.class)
                    .annotatedWith(DeviceHost.class)
                    .installedBy(this::installLifecycleComponentModule);
            deviceCodeSpec.bind(String.class)
                          .annotatedWith(DeviceCode.class)
                          .installedBy(this::installLifecycleComponentModule);
            serivceImplKeySupplier = () -> registerLifecycleComponent(LocalSlideService.class);
        } else {
            emailSpec.bind(String.class)
                     .annotatedWith(Email.class)
                     .installedBy(this::installLifecycleComponentModule);
            passwordSpec.bind(String.class)
                        .annotatedWith(Password.class)
                        .installedBy(this::installLifecycleComponentModule);
            bindExecutor();
            serivceImplKeySupplier = () -> registerLifecycleComponent(CloudSlideService.class);
        }

        positionVerificationToleranceSpec.ifPresentOrElse(
                toleranceSpec -> {
                    toleranceSpec.bind(Double.class)
                                 .annotatedWith(VerifyingSlideService.Tolerance.class)
                                 .installedBy(this::installLifecycleComponentModule);
                    bindExecutor();
                    bind(SlideService.class).annotatedWith(VerifyingSlideService.Delegate.class).to(serivceImplKeySupplier.get());
                    bind(exposedKey).to(VerifyingSlideService.class);
                },
                () -> bind(exposedKey).to(serivceImplKeySupplier.get()));
        expose(exposedKey);
    }

    private void bindExecutor() {
        if (!executorBound) {
            bind(SchedulingExecutor.class).annotatedWith(ServiceExecutor.class)
                                          .toProvider(registerLifecycleComponent(ExecutorProvider.class))
                                          .in(Singleton.class);
            executorBound = true;
        }
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<SlideService>>, HasWithAnnotation {
        private BindingSpec<String> emailSpec;
        private BindingSpec<String> passwordSpec;
        private BindingSpec<String> hostSpec;
        private SpecifiedAnnotation specifiedAnnotation = forNoAnnotation();
        private BindingSpec<Double> positionVerificationToleranceSpec;
        private BindingSpec<String> deviceCodeSpec;

        public Builder setCloutConnection(BindingSpec<String> emailSpec, BindingSpec<String> passwordSpec) {
            checkState(hostSpec == null && deviceCodeSpec == null, "setCloutConnection is mutually exclusive with setLocalConnection");
            this.emailSpec = checkNotNull(emailSpec);
            this.passwordSpec = checkNotNull(passwordSpec);
            return this;
        }

        public Builder setLocalConnection(BindingSpec<String> hostSpec, BindingSpec<String> deviceCodeSpec) {
            checkState(emailSpec == null && passwordSpec == null, "setLocalConnection is mutually exclusive with setCloutConnection");
            this.hostSpec = checkNotNull(hostSpec);
            this.deviceCodeSpec = checkNotNull(deviceCodeSpec);
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
            if (hostSpec == null) {
                checkState(emailSpec != null, "one of setCloutConnection or setLocalConnection is required");
            }

            return new SlideServiceModule(hostSpec,
                                          deviceCodeSpec,
                                          emailSpec,
                                          passwordSpec,
                                          Optional.ofNullable(positionVerificationToleranceSpec),
                                          specifiedAnnotation);
        }
    }
}
