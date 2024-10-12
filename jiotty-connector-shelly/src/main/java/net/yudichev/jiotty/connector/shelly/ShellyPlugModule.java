package net.yudichev.jiotty.connector.shelly;

import net.yudichev.jiotty.common.async.backoff.BackOffConfig;
import net.yudichev.jiotty.common.async.backoff.BackingOffExceptionHandlerModule;
import net.yudichev.jiotty.common.async.backoff.RetryableOperationExecutorModule;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.exposedBy;
import static net.yudichev.jiotty.common.inject.BindingSpec.literally;
import static net.yudichev.jiotty.common.inject.SpecifiedAnnotation.forAnnotation;

public final class ShellyPlugModule extends BaseLifecycleComponentModule implements ExposedKeyModule<ShellyPlug> {
    private final BindingSpec<String> hostSpec;

    private ShellyPlugModule(BindingSpec<String> hostSpec) {
        this.hostSpec = checkNotNull(hostSpec);
    }

    @Override
    protected void configure() {
        installLifecycleComponentModule(
                RetryableOperationExecutorModule
                        .builder()
                        .setBackingOffExceptionHandler(exposedBy(BackingOffExceptionHandlerModule
                                                                         .builder()
                                                                         .setRetryableExceptionPredicate(literally(throwable -> true))
                                                                         .withConfig(literally(BackOffConfig.builder()
                                                                                                            .setInitialInterval(Duration.ofMillis(500))
                                                                                                            .setMaxInterval(Duration.ofSeconds(5))
                                                                                                            .setMaxElapsedTime(Duration.ofSeconds(10))
                                                                                                            .build()))
                                                                         .build()))
                        .withAnnotation(forAnnotation(ShellyPlugImpl.Dependency.class))
                        .build());

        hostSpec.bind(String.class)
                .annotatedWith(ShellyPlugImpl.Host.class)
                .installedBy(this::installLifecycleComponentModule);

        bind(getExposedKey()).to(registerLifecycleComponent(ShellyPlugImpl.class));
        expose(getExposedKey());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements TypedBuilder<ShellyPlugModule> {
        private BindingSpec<String> hostSpec;

        public Builder setHost(BindingSpec<String> hostSpec) {
            this.hostSpec = checkNotNull(hostSpec);
            return this;
        }

        @Override
        public ShellyPlugModule build() {
            return new ShellyPlugModule(hostSpec);
        }
    }
}
