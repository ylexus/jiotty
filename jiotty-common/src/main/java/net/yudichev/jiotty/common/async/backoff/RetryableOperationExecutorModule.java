package net.yudichev.jiotty.common.async.backoff;

import com.google.inject.Key;
import net.yudichev.jiotty.common.inject.*;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RetryableOperationExecutorModule extends BaseLifecycleComponentModule implements ExposedKeyModule<RetryableOperationExecutor> {
    private final BindingSpec<BackingOffExceptionHandler> backingOffExceptionHandlerSpec;
    private final Key<RetryableOperationExecutor> exposedKey;

    private RetryableOperationExecutorModule(BindingSpec<BackingOffExceptionHandler> backingOffExceptionHandlerSpec, SpecifiedAnnotation specifiedAnnotation) {
        this.backingOffExceptionHandlerSpec = checkNotNull(backingOffExceptionHandlerSpec);
        exposedKey = specifiedAnnotation.specify(RetryableOperationExecutor.class);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Key<RetryableOperationExecutor> getExposedKey() {
        return exposedKey;
    }

    @Override
    protected void configure() {
        backingOffExceptionHandlerSpec.bind(BackingOffExceptionHandler.class)
                .annotatedWith(RetryableOperationExecutorImpl.Dependency.class)
                .installedBy(this::installLifecycleComponentModule);
        bind(exposedKey).to(RetryableOperationExecutorImpl.class);
        expose(exposedKey);
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<RetryableOperationExecutor>>, HasWithAnnotation {
        private BindingSpec<BackingOffExceptionHandler> backingOffExceptionHandlerSpec;
        private SpecifiedAnnotation specifiedAnnotation = SpecifiedAnnotation.forNoAnnotation();

        public Builder setBackingOffExceptionHandler(BindingSpec<BackingOffExceptionHandler> backingOffExceptionHandlerSpec) {
            this.backingOffExceptionHandlerSpec = checkNotNull(backingOffExceptionHandlerSpec);
            return this;
        }

        @Override
        public Builder withAnnotation(SpecifiedAnnotation specifiedAnnotation) {
            this.specifiedAnnotation = checkNotNull(specifiedAnnotation);
            return this;
        }

        @Override
        public ExposedKeyModule<RetryableOperationExecutor> build() {
            return new RetryableOperationExecutorModule(backingOffExceptionHandlerSpec, specifiedAnnotation);
        }
    }
}
