package net.yudichev.jiotty.common.async.backoff;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import net.yudichev.jiotty.common.inject.*;
import net.yudichev.jiotty.common.lang.TypedBuilder;
import net.yudichev.jiotty.common.lang.backoff.BackOff;

import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.literally;

public final class BackingOffExceptionHandlerModule extends BaseLifecycleComponentModule implements ExposedKeyModule<BackingOffExceptionHandler> {
    private final BindingSpec<BackOffConfig> configSpec;
    private final BindingSpec<Predicate<? super Throwable>> retryableExceptionPredicateSpec;
    private final Key<BackingOffExceptionHandler> exposedKey;

    private BackingOffExceptionHandlerModule(BindingSpec<Predicate<? super Throwable>> retryableExceptionPredicateSpec,
                                             BindingSpec<BackOffConfig> configSpec,
                                             SpecifiedAnnotation specifiedAnnotation) {
        this.retryableExceptionPredicateSpec = checkNotNull(retryableExceptionPredicateSpec);
        this.configSpec = checkNotNull(configSpec);
        exposedKey = specifiedAnnotation.specify(ExposedKeyModule.super.getExposedKey().getTypeLiteral());
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Key<BackingOffExceptionHandler> getExposedKey() {
        return exposedKey;
    }

    @Override
    protected void configure() {
        configSpec.bind(BackOffConfig.class)
                .annotatedWith(BackOffProvider.Dependency.class)
                .installedBy(this::installLifecycleComponentModule);
        bind(BackOff.class).annotatedWith(BackingOffExceptionHandlerImpl.Dependency.class).toProvider(BackOffProvider.class);
        retryableExceptionPredicateSpec.bind(new TypeLiteral<>() {})
                .annotatedWith(BackingOffExceptionHandlerImpl.Dependency.class)
                .installedBy(this::installLifecycleComponentModule);
        bind(exposedKey).to(BackingOffExceptionHandlerImpl.class);
        expose(exposedKey);
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<BackingOffExceptionHandler>>, HasWithAnnotation {
        private BindingSpec<Predicate<? super Throwable>> retryableExceptionPredicateSpec;
        private BindingSpec<BackOffConfig> configSpec = literally(BackOffConfig.builder().build());
        private SpecifiedAnnotation specifiedAnnotation = SpecifiedAnnotation.forNoAnnotation();

        public Builder setRetryableExceptionPredicate(BindingSpec<Predicate<? super Throwable>> retryableExceptionPredicateSpec) {
            this.retryableExceptionPredicateSpec = checkNotNull(retryableExceptionPredicateSpec);
            return this;
        }

        public Builder withConfig(BindingSpec<BackOffConfig> configSpec) {
            this.configSpec = checkNotNull(configSpec);
            return this;
        }

        @Override
        public Builder withAnnotation(SpecifiedAnnotation specifiedAnnotation) {
            this.specifiedAnnotation = checkNotNull(specifiedAnnotation);
            return this;
        }

        @Override
        public ExposedKeyModule<BackingOffExceptionHandler> build() {
            return new BackingOffExceptionHandlerModule(retryableExceptionPredicateSpec, configSpec, specifiedAnnotation);
        }
    }
}
