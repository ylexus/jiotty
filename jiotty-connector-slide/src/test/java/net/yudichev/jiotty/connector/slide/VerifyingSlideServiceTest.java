package net.yudichev.jiotty.connector.slide;

import net.yudichev.jiotty.common.testutil.DeterministicExecutor;
import net.yudichev.jiotty.common.time.CurrentDateTimeProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static net.yudichev.jiotty.common.lang.CompletableFutures.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
class VerifyingSlideServiceTest {
    @Test
    @MockitoSettings(strictness = LENIENT)
    void oppositeCommandsInQuickSuccessionExecutesLastCommand(@Mock SlideService delegate,
                                                              @Mock CurrentDateTimeProvider currentTimeProvider) {
        var executor = new DeterministicExecutor();
        var service = new VerifyingSlideService(delegate, () -> executor, 0.11, currentTimeProvider);

        when(delegate.getSlideInfo(0))
                .thenReturn(CompletableFuture.completedFuture(SlideInfo.of(0.0))) // moving towards 1.0: post move check 1
                .thenReturn(CompletableFuture.completedFuture(SlideInfo.of(0.1))) // moving towards 1.0: post move check 2: started moving towards 1.0
                .thenReturn(CompletableFuture.completedFuture(SlideInfo.of(0.0))); // moving back towards 0.0: returned back
        when(currentTimeProvider.currentInstant()).thenReturn(Instant.EPOCH);
        // these complete immediately in real life
        when(delegate.setSlidePosition(eq(0L), eq(1.0), any())).thenReturn(completedFuture());
        when(delegate.setSlidePosition(eq(0L), eq(0.0), any())).thenReturn(completedFuture());

        var setTo1Future = service.setSlidePosition(0, 1.0);
        var setTo2Future = service.setSlidePosition(0, 0.0);

        var inOrder = inOrder(delegate);
        inOrder.verify(delegate).setSlidePosition(eq(0L), eq(1.0), any());
        inOrder.verify(delegate).setSlidePosition(eq(0L), eq(0.0), any());

        executor.executePendingScheduledTasks();

        assertThat(setTo1Future.isDone(), is(true));
        assertThat(setTo1Future.isCompletedExceptionally(), is(false));
        assertThat(setTo2Future.isDone(), is(true));
        assertThat(setTo2Future.isCompletedExceptionally(), is(false));
    }
}