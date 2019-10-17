package net.yudichev.jiotty.common.lang;

import net.yudichev.jiotty.common.async.Scheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StabilisingConsumerTest {
    @Mock
    private Scheduler scheduler;
    @Mock
    private Consumer<String> delegate;
    @Mock
    private Closeable timeoutHandle;
    private StabilisingConsumer<String> stabilisingConsumer;

    @BeforeEach
    void setUp() {
        stabilisingConsumer = new StabilisingConsumer<>(scheduler, Duration.ofSeconds(1), delegate, "Dumbledore"::equals);
    }

    @Test
    void doesNotImmediatelyPropagateValue() {
        when(scheduler.schedule(any(), any())).thenReturn(timeoutHandle);

        stabilisingConsumer.accept("Potter");

        verify(delegate, never()).accept(any());
    }

    @Test
    void schedulesTimeout() {
        when(scheduler.schedule(any(), any())).thenReturn(timeoutHandle);

        stabilisingConsumer.accept("Potter");

        verifySchedulerSchedule();
    }

    @Test
    void propagatesValueAfterTimeout() {
        when(scheduler.schedule(any(), any())).thenReturn(timeoutHandle);

        stabilisingConsumer.accept("Potter");
        verifySchedulerSchedule().run();

        verify(delegate).accept("Potter");
    }

    @Test
    void cancelsTimerIfNewValueArrives() {
        when(scheduler.schedule(any(), any())).thenReturn(timeoutHandle);

        stabilisingConsumer.accept("Potter");
        verifySchedulerSchedule();

        stabilisingConsumer.accept("Harry");
        verify(timeoutHandle).close();
    }

    @Test
    void propagatesModifiedValueIfChangedBeforeTimeout() {
        when(scheduler.schedule(any(), any())).thenReturn(timeoutHandle);
        stabilisingConsumer.accept("Potter");
        verifySchedulerSchedule();

        reset(scheduler);
        stabilisingConsumer.accept("Harry");
        verifySchedulerSchedule().run();

        verify(delegate).accept("Harry");
    }

    @Test
    void doesNotStabilisePredicateMatchingValue() {
        stabilisingConsumer.accept("Dumbledore");

        verify(delegate).accept("Dumbledore");
        verify(scheduler, never()).schedule(any(), any());
    }

    @Test
    void stabilisesValueAfterPredicateMatchingValue() {
        when(scheduler.schedule(any(), any())).thenReturn(timeoutHandle);
        stabilisingConsumer.accept("Dumbledore");
        stabilisingConsumer.accept("Harry");

        Runnable timerTask = verifySchedulerSchedule();
        verify(delegate, never()).accept("Harry");

        timerTask.run();
        verify(delegate).accept("Harry");
    }

    private Runnable verifySchedulerSchedule() {
        ArgumentCaptor<Runnable> timeoutHandlerCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(eq(Duration.ofSeconds(1)), timeoutHandlerCaptor.capture());
        return timeoutHandlerCaptor.getValue();
    }
}