package net.yudichev.jiotty.appliance;

import com.google.inject.Key;
import net.yudichev.jiotty.common.async.backoff.BackOffConfig;
import net.yudichev.jiotty.common.async.backoff.BackingOffExceptionHandlerModule;
import net.yudichev.jiotty.common.async.backoff.RetryableOperationExecutorModule;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.inject.SpecifiedAnnotation;
import net.yudichev.jiotty.common.lang.Optionals;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.exposedBy;
import static net.yudichev.jiotty.common.inject.BindingSpec.literally;
import static net.yudichev.jiotty.common.inject.GuiceUtil.uniqueAnnotation;
import static net.yudichev.jiotty.common.inject.SpecifiedAnnotation.forAnnotation;

public abstract class ApplianceModule extends BaseLifecycleComponentModule implements ExposedKeyModule<Appliance> {
    private final Key<Appliance> exposedKey;
    private final Optional<BindingSpec<BackOffConfig>> backoffConfigSpec;

    protected ApplianceModule(SpecifiedAnnotation targetAnnotation) {
        this(targetAnnotation, Optional.empty());
    }

    protected ApplianceModule(SpecifiedAnnotation targetAnnotation, BindingSpec<BackOffConfig> backoffConfigSpec) {
        this(targetAnnotation, Optional.of(backoffConfigSpec));
    }

    protected ApplianceModule(SpecifiedAnnotation targetAnnotation, Optional<BindingSpec<BackOffConfig>> backoffConfigSpec) {
        exposedKey = targetAnnotation.specify(Appliance.class);
        this.backoffConfigSpec = checkNotNull(backoffConfigSpec);
    }

    @Override
    public final Key<Appliance> getExposedKey() {
        return exposedKey;
    }

    @Override
    protected final void configure() {
        Key<? extends Appliance> implKey = configureDependencies();
        Optionals
                .ifPresent(backoffConfigSpec, spec -> {
                    installLifecycleComponentModule(RetryableOperationExecutorModule.builder()
                            .setBackingOffExceptionHandler(exposedBy(BackingOffExceptionHandlerModule.builder()
                                    .setRetryableExceptionPredicate(literally(throwable -> true))
                                    .withAnnotation(forAnnotation(uniqueAnnotation()))
                                    .withConfig(spec)
                                    .build()))
                            .withAnnotation(forAnnotation(RetryingAppliance.Dependency.class))
                            .build());
                    bind(Appliance.class).annotatedWith(RetryingAppliance.Dependency.class).to(implKey);
                    bind(exposedKey).to(RetryingAppliance.class);
                })
                .orElse(() -> bind(exposedKey).to(implKey));

        expose(exposedKey);
    }

    protected abstract Key<? extends Appliance> configureDependencies();
}
