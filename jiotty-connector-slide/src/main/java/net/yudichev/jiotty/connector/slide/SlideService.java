package net.yudichev.jiotty.connector.slide;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

public interface SlideService {
    CompletableFuture<SlideInfo> getSlideInfo(long slideId, Executor executor);

    default CompletableFuture<SlideInfo> getSlideInfo(long slideId) {
        return getSlideInfo(slideId, directExecutor()); // because OkHttp calls are async
    }


    CompletableFuture<Void> setSlidePosition(long slideId, double position, Executor executor);

    default CompletableFuture<Void> setSlidePosition(long slideId, double position) {
        return setSlidePosition(slideId, position, directExecutor());  // because OkHttp calls are async
    }

    default CompletableFuture<Void> openSlide(long slideId, Executor executor) {
        return setSlidePosition(slideId, 0, executor);
    }

    default CompletableFuture<Void> openSlide(long slideId) {
        return openSlide(slideId, directExecutor()); // because OkHttp calls are async
    }

    default CompletableFuture<Void> closeSlide(long slideId, Executor executor) {
        return setSlidePosition(slideId, 1, executor);
    }

    default CompletableFuture<Void> closeSlide(long slideId) {
        return closeSlide(slideId, directExecutor()); // because OkHttp calls are async
    }
}
