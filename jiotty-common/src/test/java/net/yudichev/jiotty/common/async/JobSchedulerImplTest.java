package net.yudichev.jiotty.common.async;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class JobSchedulerImplTest {

    private ProgrammableClock clock;
    private JobSchedulerImpl scheduler;
    private int execCount;
    private Runnable task;
    private Duration taskRunTime = Duration.ZERO;

    @BeforeEach
    void setUp() {
        clock = new ProgrammableClock().withMdc().withTasksSeeingTargetTime(true);
        scheduler = new JobSchedulerImpl(clock, clock);
        scheduler.start();
        task = () -> {
            clock.advanceTime(taskRunTime);
            execCount++;
        };
    }

    @Test
    void daily() {
        var jobHandle = scheduler.daily("dailyJob", LocalTime.of(9, 0), task);
        clock.tick();
        assertThat(execCount, is(0));

        clock.setTimeAndTick(Instant.EPOCH.plus(8, HOURS));
        assertThat(execCount, is(0));

        clock.setTimeAndTick(Instant.EPOCH.plus(9, HOURS));
        assertThat(execCount, is(1));

        clock.setTimeAndTick(Instant.EPOCH.plus(1, DAYS).plus(8, HOURS));
        assertThat(execCount, is(1));

        clock.setTimeAndTick(Instant.EPOCH.plus(1, DAYS).plus(9, HOURS));
        assertThat(execCount, is(2));

        jobHandle.close();
        clock.setTimeAndTick(Instant.EPOCH.plus(10, DAYS));
        assertThat(execCount, is(2));
    }

    @Test
    void daily_taskOverruns() {
        scheduler.daily("dailyJob", LocalTime.of(9, 0), task);
        // scheduled: 1 jan 9:00 << effectively overruns by 3 days 5 hours
        // scheduled: 2 jan 9:00 << skipped
        // scheduled: 3 jan 9:00 << skipped
        // scheduled: 4 jan 9:00 << executed
        // ...

        taskRunTime = Duration.ofDays(2).plusHours(5);
        clock.setTimeAndTick(Instant.EPOCH.plus(1, DAYS));
        // ticked to 2 jan 00:00, executed 1 task which advances to 4 jan 5:00
        assertThat(execCount, is(1));

        // next task will overrun
        taskRunTime = Duration.ZERO;
        // now next task should execute on 4 jan 9:00
        clock.tick();
        assertThat(execCount, is(1));
        clock.setTimeAndTick(Instant.EPOCH.plus(3, DAYS).plus(9, HOURS));
        assertThat(execCount, is(2));
    }

    @Test
    void monthly() {
        var jobHandle = scheduler.monthly("monthlyJob", 2, task);
        clock.tick();
        assertThat(execCount, is(0));

        clock.setTimeAndTick(Instant.parse("1970-01-02T00:02:00Z"));
        assertThat(execCount, is(0));

        clock.setTimeAndTick(Instant.parse("1970-01-02T03:00:00Z"));
        assertThat(execCount, is(1));

        clock.setTimeAndTick(Instant.parse("1970-02-02T02:00:00Z"));
        assertThat(execCount, is(1));

        clock.setTimeAndTick(Instant.parse("1970-02-02T03:00:00Z"));
        assertThat(execCount, is(2));

        jobHandle.close();
        clock.setTimeAndTick(Instant.parse("1970-05-01T00:00:00Z"));
        assertThat(execCount, is(2));
    }
}