package net.yudichev.jiotty.connector.slide;

import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.time.CurrentDateTimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.yudichev.jiotty.connector.slide.Bindings.ServiceExecutor;
import static net.yudichev.jiotty.connector.slide.Constants.POSITION_TOLERANCE;

final class VerifyingSlideService implements SlideService {
    private static final Logger logger = LoggerFactory.getLogger(VerifyingSlideService.class);
    private static final Duration POSITION_POLL_PERIOD = Duration.ofSeconds(2);
    private static final Duration POSITION_VERIFY_TIMEOUT = Duration.ofSeconds(30);

    private final SlideService delegate;
    private final Provider<SchedulingExecutor> executorProvider;
    private final CurrentDateTimeProvider currentDateTimeProvider;

    @Inject VerifyingSlideService(@Delegate SlideService delegate,
                                  @ServiceExecutor Provider<SchedulingExecutor> executorProvider,
                                  CurrentDateTimeProvider currentDateTimeProvider) {
        this.delegate = checkNotNull(delegate);
        this.executorProvider = checkNotNull(executorProvider);
        this.currentDateTimeProvider = checkNotNull(currentDateTimeProvider);
    }

    @Override
    public CompletableFuture<SlideInfo> getSlideInfo(long slideId) {
        return delegate.getSlideInfo(slideId);
    }

    @Override
    public CompletableFuture<Void> setSlidePosition(long slideId, double position) {
        return delegate.setSlidePosition(slideId, position)
                .thenCompose(unused -> waitForPosition(slideId, position));
    }

    private CompletableFuture<Void> waitForPosition(long slideId, double targetPosition) {
        return new TargetPosition(slideId, targetPosition).result;
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME) @interface Delegate {
    }

    private final class TargetPosition {
        private final long slideId;
        private final double targetPosition;
        private final Instant deadline;
        private final CompletableFuture<Void> result;

        private TargetPosition(long slideId, double targetPosition) {
            this.slideId = slideId;
            this.targetPosition = targetPosition;
            result = new CompletableFuture<>();
            deadline = currentDateTimeProvider.currentInstant().plus(POSITION_VERIFY_TIMEOUT);
            logger.debug("Slide {}: deadline to reach position {} is {}", slideId, targetPosition, deadline);
            await();
        }

        private void await() {
            logger.debug("Side {}: will poll for position in {}", slideId, POSITION_POLL_PERIOD);
            executorProvider.get().schedule(POSITION_POLL_PERIOD, this::pollPosition);
        }

        private void pollPosition() {
            delegate.getSlideInfo(slideId)
                    .whenComplete((slideInfo, e) -> {
                        if (e == null) {
                            double currentPosition = slideInfo.position();
                            logger.debug("Slide {}: current pos {}, target pos {}", slideId, currentPosition, targetPosition);
                            if (Math.abs(currentPosition - targetPosition) < POSITION_TOLERANCE) {
                                logger.debug("Side {} reached satisfiable position", slideId);
                                result.complete(null);
                            } else if (currentDateTimeProvider.currentInstant().isAfter(deadline)) {
                                result.completeExceptionally(new RuntimeException(
                                        "Timed out verifying position of slide " + slideInfo + " after, target position " + targetPosition +
                                                ", current position " + currentPosition));
                            } else {
                                await();
                            }
                        } else {
                            result.completeExceptionally(new RuntimeException("Failed to verify position of slide " + slideInfo + ": status poll failed", e));
                        }
                    });
        }
    }
}