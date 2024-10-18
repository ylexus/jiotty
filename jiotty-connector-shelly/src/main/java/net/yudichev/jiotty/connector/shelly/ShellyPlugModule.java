package net.yudichev.jiotty.connector.shelly;

import com.google.inject.Key;
import net.yudichev.jiotty.common.async.backoff.BackOffConfig;
import net.yudichev.jiotty.common.async.backoff.BackingOffExceptionHandlerModule;
import net.yudichev.jiotty.common.async.backoff.RetryableOperationExecutorModule;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
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

public final class ShellyPlugModule extends BaseLifecycleComponentModule implements ExposedKeyModule<ShellyPlug> {
    private final BindingSpec<String> hostSpec;
    private final Key<ShellyPlug> exposedKey;

    private ShellyPlugModule(BindingSpec<String> hostSpec, SpecifiedAnnotation specifiedAnnotation) {
        this.hostSpec = checkNotNull(hostSpec);
        exposedKey = specifiedAnnotation.specify(ExposedKeyModule.super.getExposedKey().getTypeLiteral());
    }

    @Override
    public Key<ShellyPlug> getExposedKey() {
        return exposedKey;
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
                                                                                                            .setMaxInterval(Duration.ofSeconds(1))
                                                                                                            .setMaxElapsedTime(Duration.ofSeconds(5))
                                                                                                            .build()))
                                                                         .build()))
                        .withAnnotation(forAnnotation(ShellyPlugImpl.Dependency.class))
                        .build());

        hostSpec.bind(String.class)
                .annotatedWith(ShellyPlugImpl.Host.class)
                .installedBy(this::installLifecycleComponentModule);

        bind(exposedKey).to(registerLifecycleComponent(ShellyPlugImpl.class));
        expose(exposedKey);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements TypedBuilder<ShellyPlugModule>, HasWithAnnotation {
        private BindingSpec<String> hostSpec;
        private SpecifiedAnnotation specifiedAnnotation = SpecifiedAnnotation.forNoAnnotation();

        public Builder setHost(BindingSpec<String> hostSpec) {
            this.hostSpec = checkNotNull(hostSpec);
            return this;
        }

        @Override
        public Builder withAnnotation(SpecifiedAnnotation specifiedAnnotation) {
            this.specifiedAnnotation = checkNotNull(specifiedAnnotation);
            return this;
        }

        @Override
        public ShellyPlugModule build() {
            return new ShellyPlugModule(hostSpec, specifiedAnnotation);
        }
    }
}
