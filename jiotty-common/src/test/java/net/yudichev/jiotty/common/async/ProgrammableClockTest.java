package net.yudichev.jiotty.common.async;

import net.yudichev.jiotty.common.lang.Closeable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgrammableClockTest {

    @Mock
    private Runnable task;
    private ProgrammableClock clock;
    private SchedulingExecutor executor;

    @BeforeEach
    void setUp() {
        clock = new ProgrammableClock();
        executor = clock.createSingleThreadedSchedulingExecutor("threadName");
    }

    @Test
    void executeSimple() {
        executor.execute(task);
        verify(task, never()).run();

        clock.tick();
        verify(task).run();
    }

    @Test
    void executeNested() {
        executor.execute(() -> executor.execute(task));
        verify(task, never()).run();

        clock.tick();
        verify(task).run();
    }

    @Test
    void scheduleSimple() {
        executor.schedule(Duration.ofSeconds(1), task);
        verify(task, never()).run();

        clock.tick();
        verify(task, never()).run();

        clock.advanceTimeAndTick(Duration.ofMillis(500));
        verify(task, never()).run();

        clock.advanceTimeAndTick(Duration.ofSeconds(1));
        verify(task).run();

        clock.advanceTimeAndTick(Duration.ofSeconds(1));
        verifyNoMoreInteractions(task);
    }

    @Test
    void scheduleNestedInExecute() {
        executor.execute(() -> executor.schedule(Duration.ofSeconds(1), task));
        verify(task, never()).run();

        clock.tick();
        verify(task, never()).run();

        clock.advanceTimeAndTick(Duration.ofSeconds(1));
        verify(task).run();
    }

    @Test
    void executeNestedInSchedule() {
        executor.schedule(Duration.ofSeconds(1), () -> executor.execute(task));
        verify(task, never()).run();

        clock.tick();
        verify(task, never()).run();

        clock.advanceTimeAndTick(Duration.ofSeconds(1));
        verify(task).run();
    }

    @Test
    void scheduleSimpleDoesNotFireIfClosed() {
        Closeable schedule = executor.schedule(Duration.ofSeconds(1), task);
        clock.tick();

        schedule.close();

        clock.advanceTimeAndTick(Duration.ofSeconds(1));
        verify(task, never()).run();
    }

    @Test
    void scheduleAtFixedRateSimple() {
        Closeable schedule = executor.scheduleAtFixedRate(Duration.ofSeconds(3), Duration.ofSeconds(1), task);
        clock.tick();
        verify(task, never()).run();

        clock.setTimeAndTick(Instant.ofEpochMilli(2500));
        verify(task, never()).run();

        clock.setTimeAndTick(Instant.ofEpochMilli(3000));
        verify(task).run();
        reset(task);

        clock.setTimeAndTick(Instant.ofEpochMilli(3500));
        verify(task, never()).run();

        clock.setTimeAndTick(Instant.ofEpochMilli(4000));
        verify(task).run();
        reset(task);

        clock.setTimeAndTick(Instant.ofEpochMilli(4500));
        verify(task, never()).run();

        clock.setTimeAndTick(Instant.ofEpochMilli(5000));
        verify(task).run();
        reset(task);

        schedule.close();
        clock.setTimeAndTick(Instant.ofEpochMilli(7000));
        verify(task, never()).run();
    }

    @Test
    void scheduleAtFixedRateDeeplyNested() {
        /*
          0000 root   : schedule inner 1 every 1000
          1000 inner 1: schedule task 1 every 500
          1500 task 1 : RUN
          2000 inner 1: schedule task 2 every 500
               task 1 : RUN
          2500 task 1 : RUN
               task 2 : RUN
          3000 inner 1: schedule task 3 every 500
               task 1 : RUN
               task 2 : RUN
          3500 task 1 : RUN
               task 2 : RUN
               task 3 : RUN
         */
        executor.execute(() -> executor.scheduleAtFixedRate(Duration.ofSeconds(1),
                () -> executor.execute(() -> executor.scheduleAtFixedRate(Duration.ofMillis(500), task))));

        clock.setTimeAndTick(Instant.ofEpochMilli(1000));
        verify(task, never()).run();

        clock.setTimeAndTick(Instant.ofEpochMilli(1500));
        verify(task).run();
        reset(task);

        clock.setTimeAndTick(Instant.ofEpochMilli(2000));
        verify(task).run();
        reset(task);

        clock.setTimeAndTick(Instant.ofEpochMilli(2500));
        verify(task, times(2)).run();
        reset(task);

        clock.setTimeAndTick(Instant.ofEpochMilli(3000));
        verify(task, times(2)).run();
        reset(task);

        clock.setTimeAndTick(Instant.ofEpochMilli(3500));
        verify(task, times(3)).run();
    }

    @Test
    void nestedClosingOfScheduledTask() {
        Closeable schedule = executor.schedule(Duration.ofSeconds(2), task);
        executor.schedule(Duration.ofSeconds(1), schedule::close);

        clock.advanceTimeAndTick(Duration.ofSeconds(3));
        verify(task, never()).run();
    }

    @Test
    void scheduleSingleTaskAfterFewFixedRateTasks_executesAllInRightOrder(@Mock Runnable task2) {
        executor.scheduleAtFixedRate(Duration.ofSeconds(1), task);
        executor.schedule(Duration.ofSeconds(3), task2);

        clock.advanceTimeAndTick(Duration.ofSeconds(3));
        InOrder inOrder = inOrder(task, task2);
        inOrder.verify(task, times(2)).run();
        inOrder.verify(task2).run();
        inOrder.verify(task).run();
    }

    @Test
    void closeScheduledTaskAfterItsRun() {
        Closeable schedule = executor.schedule(Duration.ofSeconds(1), task);
        clock.advanceTimeAndTick(Duration.ofSeconds(1));
        schedule.close();
    }

    @Test
    void closingOneExecutorCancelsItsTasks_sameTime(@Mock Runnable task2) {
        SchedulingExecutor executor2 = clock.createSingleThreadedSchedulingExecutor("executor2");
        executor.schedule(Duration.ofSeconds(1), task);
        executor2.schedule(Duration.ofSeconds(1), task2);
        clock.tick();

        executor2.close();

        clock.advanceTimeAndTick(Duration.ofSeconds(1));
        verify(task).run();
        verify(task2, never()).run();
    }

    @Test
    void closingOneExecutorCancelsItsTasks_diffTime(@Mock Runnable task2) {
        SchedulingExecutor executor2 = clock.createSingleThreadedSchedulingExecutor("executor2");
        executor.schedule(Duration.ofSeconds(1), task);
        executor2.schedule(Duration.ofSeconds(2), task2);
        clock.tick();

        executor2.close();

        clock.advanceTimeAndTick(Duration.ofSeconds(2));
        verify(task).run();
        verify(task2, never()).run();
    }
}