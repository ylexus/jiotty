package net.yudichev.jiotty.common.async;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

public interface AsyncOperationRetry {
    <T> CompletableFuture<T> withBackOffAndRetry(String operationName,
                                                 Supplier<? extends CompletableFuture<T>> action,
                                                 BiConsumer<? super Long, Runnable> backoffHandler);

    default <T> CompletableFuture<T> withBackOffAndRetry(String operationName,
                                                         Supplier<? extends CompletableFuture<T>> action,
                                                         SchedulingExecutor retryScheduler) {
        return withBackOffAndRetry(operationName, action, (delayMs, retryTask) -> retryScheduler.schedule(Duration.ofMillis(delayMs), retryTask));
    }

    default <T> CompletableFuture<T> withBackOffAndRetry(String operationName,
                                                         Supplier<? extends CompletableFuture<T>> action,
                                                         LongConsumer backoffHandler) {
        return withBackOffAndRetry(operationName, action, (delayMs, retry) -> {
            backoffHandler.accept(delayMs);
            retry.run();
        });
    }

    default <T> CompletableFuture<T> withBackOffAndRetry(String operationName,
                                                         Supplier<? extends CompletableFuture<T>> action) {
        return withBackOffAndRetry(operationName, action, ignored -> {});
    }
}
