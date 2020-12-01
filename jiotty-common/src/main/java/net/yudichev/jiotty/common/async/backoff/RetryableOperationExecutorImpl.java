package net.yudichev.jiotty.common.async.backoff;

import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.common.lang.CompletableFutures;
import net.yudichev.jiotty.common.lang.Either;
import net.yudichev.jiotty.common.lang.PackagePrivateImmutablesStyle;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

final class RetryableOperationExecutorImpl implements RetryableOperationExecutor {
    private static final Logger logger = LoggerFactory.getLogger(RetryableOperationExecutorImpl.class);
    private final BackingOffExceptionHandler backingOffExceptionHandler;

    @Inject
    RetryableOperationExecutorImpl(@Dependency BackingOffExceptionHandler backingOffExceptionHandler) {
        this.backingOffExceptionHandler = checkNotNull(backingOffExceptionHandler);
    }

    @Override
    public <T> CompletableFuture<T> withBackOffAndRetry(String operationName,
                                                        Supplier<? extends CompletableFuture<T>> action,
                                                        LongConsumer backoffEventConsumer) {
        return action.get()
                .thenApply(value -> {
                    backingOffExceptionHandler.reset();
                    return Either.<T, RetryableFailure>left(value);
                })
                .exceptionally(exception -> {
                    Optional<Long> backoffDelayMs = backingOffExceptionHandler.handle(operationName, exception);
                    return Either.right(RetryableFailure.of(exception, backoffDelayMs));
                })
                .thenCompose(eitherValueOrRetryableFailure -> eitherValueOrRetryableFailure.map(
                        CompletableFuture::completedFuture,
                        retryableFailure -> retryableFailure.backoffDelayMs()
                                .map(backoffDelayMs -> {
                                    logger.debug("Retrying operation '{}' with backoff {}ms", operationName, retryableFailure.backoffDelayMs());
                                    backoffEventConsumer.accept(backoffDelayMs);
                                    return withBackOffAndRetry(operationName, action, backoffEventConsumer);
                                })
                                .orElseGet(() -> CompletableFutures.failure(retryableFailure.exception()))
                ));
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }

    @Value.Immutable
    @PackagePrivateImmutablesStyle
    interface BaseRetryableFailure {
        @Value.Parameter
        Throwable exception();

        @Value.Parameter
        Optional<Long> backoffDelayMs();
    }
}
