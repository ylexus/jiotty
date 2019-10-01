package net.jiotty.common.lang;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.stream.Collector;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static net.jiotty.common.lang.DelayedExecutors.delayedExecutor;

public final class CompletableFutures {
    public static <T> CompletableFuture<T> completable(Future<T> future) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<Void> allOf(ImmutableList<CompletableFuture<?>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public static <T> CompletableFuture<T> completedFuture() {
        return CompletableFuture.completedFuture(null);
    }

    public static CompletableFuture<Void> delay(long millis) {
        return CompletableFuture.runAsync(() -> {}, delayedExecutor(millis));
    }

    public static <T> CompletableFuture<T> failure(String message) {
        return failure(new RuntimeException(message));
    }

    public static <T> CompletableFuture<T> failure(Exception exception) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(exception);
        return future;
    }

    public static <T> Collector<CompletableFuture<T>, ?, CompletableFuture<List<T>>> toFutureOfList() {
        return Collector.of(
                ImmutableList::<CompletableFuture<T>>builder,
                ImmutableList.Builder::add,
                (builder1, builder2) ->
                        ImmutableList.<CompletableFuture<T>>builder()
                                .addAll(builder1.build())
                                .addAll(builder2.build()),
                builder -> {
                    ImmutableList<CompletableFuture<T>> listOfFutures = builder.build();
                    return CompletableFuture.allOf(listOfFutures.toArray(new CompletableFuture[0]))
                            .thenApply(ignored -> unmodifiableList(listOfFutures.stream()
                                    .map(CompletableFuture::join)
                                    // cannot use ImmutableList here, void futures typically return nulls
                                    .collect(toList())));
                }
        );
    }

    public static <T> BiConsumer<T, Throwable> logErrorOnFailure(Logger logger, String errorMessageTemplate, Object... params) {
        return (aVoid, e) -> {
            if (e != null) {
                logger.error(String.format(errorMessageTemplate, params), e);
            }
        };
    }
}
