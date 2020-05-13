package net.yudichev.jiotty.connector.slide;

import java.util.concurrent.CompletableFuture;

public interface SlideService {
    CompletableFuture<SlideInfo> getSlideInfo(long slideId);

    CompletableFuture<Void> setSlidePosition(long slideId, double position);

    default CompletableFuture<Void> openSlide(long slideId) {
        return setSlidePosition(slideId, 0);
    }

    default CompletableFuture<Void> closeSlide(long slideId) {
        return setSlidePosition(slideId, 1);
    }
}
