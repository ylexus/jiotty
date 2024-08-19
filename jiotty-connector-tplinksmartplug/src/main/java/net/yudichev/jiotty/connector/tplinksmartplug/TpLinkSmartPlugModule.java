package net.yudichev.jiotty.connector.tplinksmartplug;

import com.google.inject.Key;
import net.yudichev.jiotty.appliance.Appliance;
import net.yudichev.jiotty.appliance.ApplianceModule;
import net.yudichev.jiotty.common.async.backoff.BackOffConfig;
import net.yudichev.jiotty.common.async.backoff.BackingOffExceptionHandlerModule;
import net.yudichev.jiotty.common.async.backoff.RetryableOperationExecutorModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.inject.HasWithAnnotation;
import net.yudichev.jiotty.common.inject.SpecifiedAnnotation;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.exposedBy;
import static net.yudichev.jiotty.common.inject.BindingSpec.literally;
import static net.yudichev.jiotty.common.inject.SpecifiedAnnotation.forAnnotation;
import static net.yudichev.jiotty.common.inject.SpecifiedAnnotation.forNoAnnotation;

public final class TpLinkSmartPlugModule extends ApplianceModule {
    private final BindingSpec<String> usernameSpec;
    private final BindingSpec<String> passwordSpec;
    private final BindingSpec<String> termIdSpec;
    private final BindingSpec<String> deviceIdSpec;
    private final BindingSpec<String> nameSpec;

    private TpLinkSmartPlugModule(BindingSpec<String> usernameSpec,
                                  BindingSpec<String> passwordSpec,
                                  BindingSpec<String> termIdSpec,
                                  BindingSpec<String> deviceIdSpec,
                                  BindingSpec<String> nameSpec,
                                  SpecifiedAnnotation targetAnnotation) {
        super(targetAnnotation);
        this.usernameSpec = checkNotNull(usernameSpec);
        this.passwordSpec = checkNotNull(passwordSpec);
        this.termIdSpec = checkNotNull(termIdSpec);
        this.deviceIdSpec = checkNotNull(deviceIdSpec);
        this.nameSpec = checkNotNull(nameSpec);
    }


    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected Key<? extends Appliance> configureDependencies() {
        usernameSpec.bind(String.class)
                    .annotatedWith(TpLinkSmartPlug.Username.class)
                    .installedBy(this::installLifecycleComponentModule);
        passwordSpec.bind(String.class)
                    .annotatedWith(TpLinkSmartPlug.Password.class)
                    .installedBy(this::installLifecycleComponentModule);
        termIdSpec.bind(String.class)
                  .annotatedWith(TpLinkSmartPlug.TermId.class)
                  .installedBy(this::installLifecycleComponentModule);
        deviceIdSpec.bind(String.class)
                    .annotatedWith(TpLinkSmartPlug.DeviceId.class)
                    .installedBy(this::installLifecycleComponentModule);
        nameSpec.bind(String.class)
                .annotatedWith(TpLinkSmartPlug.Name.class)
                .installedBy(this::installLifecycleComponentModule);
        installLifecycleComponentModule(
                RetryableOperationExecutorModule
                        .builder()
                        .withAnnotation(forAnnotation(TpLinkSmartPlug.Dependency.class))
                        .setBackingOffExceptionHandler(
                                exposedBy(BackingOffExceptionHandlerModule
                                                  .builder()
                                                  .setRetryableExceptionPredicate(literally(throwable -> true))
                                                  .withConfig(literally(BackOffConfig.builder().setInitialInterval(Duration.ofMillis(500))
                                                                                     .setMaxInterval(Duration.ofSeconds(30))
                                                                                     .setMaxElapsedTime(Duration.ofHours(3))
                                                                                     .build()))
                                                  .build()))
                        .build());
        return registerLifecycleComponent(TpLinkSmartPlug.class);
    }

    public static class Builder implements TypedBuilder<ExposedKeyModule<Appliance>>, HasWithAnnotation {
        private BindingSpec<String> usernameSpec;
        private BindingSpec<String> passwordSpec;
        private BindingSpec<String> termIdSpec;
        private BindingSpec<String> deviceIdSpec;
        private BindingSpec<String> nameSpec;
        private SpecifiedAnnotation specifiedAnnotation = forNoAnnotation();

        public Builder setUsername(BindingSpec<String> usernameSpec) {
            this.usernameSpec = checkNotNull(usernameSpec);
            return this;
        }

        public Builder setPassword(BindingSpec<String> passwordSpec) {
            this.passwordSpec = passwordSpec;
            return this;
        }

        public Builder setTermId(BindingSpec<String> termIdSpec) {
            this.termIdSpec = termIdSpec;
            return this;
        }

        public Builder setDeviceId(BindingSpec<String> deviceIdSpec) {
            this.deviceIdSpec = checkNotNull(deviceIdSpec);
            return this;
        }

        public Builder withName(BindingSpec<String> nameSpec) {
            this.nameSpec = checkNotNull(nameSpec);
            return this;
        }

        @Override
        public Builder withAnnotation(SpecifiedAnnotation specifiedAnnotation) {
            this.specifiedAnnotation = checkNotNull(specifiedAnnotation);
            return this;
        }

        @Override
        public ExposedKeyModule<Appliance> build() {
            if (nameSpec == null) {
                nameSpec = deviceIdSpec;
            }
            return new TpLinkSmartPlugModule(usernameSpec, passwordSpec, termIdSpec, deviceIdSpec, nameSpec, specifiedAnnotation);
        }
    }
}
