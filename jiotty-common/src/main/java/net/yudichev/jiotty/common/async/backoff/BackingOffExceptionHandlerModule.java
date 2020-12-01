package net.yudichev.jiotty.common.async.backoff;

import com.google.inject.TypeLiteral;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;
import net.yudichev.jiotty.common.lang.backoff.BackOff;

import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.literally;

public final class BackingOffExceptionHandlerModule extends BaseLifecycleComponentModule implements ExposedKeyModule<BackingOffExceptionHandler> {
    private final BindingSpec<BackOffConfig> configSpec;
    private final BindingSpec<Predicate<? super Throwable>> retryableExceptionPredicateSpec;

    private BackingOffExceptionHandlerModule(BindingSpec<Predicate<? super Throwable>> retryableExceptionPredicateSpec,
                                             BindingSpec<BackOffConfig> configSpec) {
        this.retryableExceptionPredicateSpec = checkNotNull(retryableExceptionPredicateSpec);
        this.configSpec = checkNotNull(configSpec);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        configSpec.bind(BackOffConfig.class)
                .annotatedWith(BackOffProvider.Dependency.class)
                .installedBy(this::installLifecycleComponentModule);
        bind(BackOff.class).annotatedWith(BackingOffExceptionHandlerImpl.Dependency.class).toProvider(BackOffProvider.class);
        retryableExceptionPredicateSpec.bind(new TypeLiteral<Predicate<? super Throwable>>() {})
                .annotatedWith(BackingOffExceptionHandlerImpl.Dependency.class)
                .installedBy(this::installLifecycleComponentModule);
        bind(getExposedKey()).to(BackingOffExceptionHandlerImpl.class);
        expose(getExposedKey());
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<BackingOffExceptionHandler>> {
        private BindingSpec<Predicate<? super Throwable>> retryableExceptionPredicateSpec;
        private BindingSpec<BackOffConfig> configSpec = literally(BackOffConfig.builder().build());

        public Builder setRetryableExceptionPredicate(BindingSpec<Predicate<? super Throwable>> retryableExceptionPredicateSpec) {
            this.retryableExceptionPredicateSpec = checkNotNull(retryableExceptionPredicateSpec);
            return this;
        }

        public Builder withConfig(BindingSpec<BackOffConfig> configSpec) {
            this.configSpec = checkNotNull(configSpec);
            return this;
        }

        @Override
        public ExposedKeyModule<BackingOffExceptionHandler> build() {
            return new BackingOffExceptionHandlerModule(retryableExceptionPredicateSpec, configSpec);
        }
    }
}
