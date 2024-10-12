package net.yudichev.jiotty.common.async.backoff;

import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.common.lang.CompletableFutures;
import net.yudichev.jiotty.common.lang.Either;
import net.yudichev.jiotty.common.lang.PackagePrivateImmutablesStyle;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

final class RetryableOperationExecutorImpl implements RetryableOperationExecutor {
    private static final Logger logger = LoggerFactory.getLogger(RetryableOperationExecutorImpl.class);
    private final Provider<BackingOffExceptionHandler> exceptionHandlerProvider;

    @Inject
    RetryableOperationExecutorImpl(@Dependency Provider<BackingOffExceptionHandler> exceptionHandlerProvider) {
        this.exceptionHandlerProvider = checkNotNull(exceptionHandlerProvider);
    }

    @Override
    public <T> CompletableFuture<T> withBackOffAndRetry(String operationName,
                                                        Supplier<? extends CompletableFuture<T>> action,
                                                        LongConsumer backoffEventConsumer) {
        var exceptionHandler = exceptionHandlerProvider.get();
        logger.debug("Executing operation '{}' with retries using handler {}", operationName, exceptionHandler);
        return doWithBackOffAndRetry(operationName, action, backoffEventConsumer, exceptionHandler);
    }

    private static <T> CompletableFuture<T> doWithBackOffAndRetry(String operationName,
                                                                  Supplier<? extends CompletableFuture<T>> action,
                                                                  LongConsumer backoffEventConsumer,
                                                                  BackingOffExceptionHandler exceptionHandler) {
        return action.get()
                     .thenApply(Either::<T, RetryableFailure>left)
                     .exceptionally(exception -> {
                         Optional<Long> backoffDelayMs = exceptionHandler.handle(operationName, exception);
                         return Either.right(RetryableFailure.of(exception, backoffDelayMs));
                     })
                     .thenCompose(eitherValueOrRetryableFailure -> eitherValueOrRetryableFailure.map(
                             CompletableFuture::completedFuture,
                             retryableFailure -> retryableFailure.backoffDelayMs()
                                                                 .map(backoffDelayMs -> {
                                                                     logger.debug("Retrying operation '{}' with backoff {}ms",
                                                                                  operationName,
                                                                                  retryableFailure.backoffDelayMs());
                                                                     backoffEventConsumer.accept(backoffDelayMs);
                                                                     return doWithBackOffAndRetry(operationName,
                                                                                                  action,
                                                                                                  backoffEventConsumer,
                                                                                                  exceptionHandler);
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
