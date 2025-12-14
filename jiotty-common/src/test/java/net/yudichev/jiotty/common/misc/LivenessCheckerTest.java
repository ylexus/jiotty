package net.yudichev.jiotty.common.misc;

import net.yudichev.jiotty.common.async.ProgrammableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.time.Instant.EPOCH;
import static java.time.temporal.ChronoUnit.MINUTES;
import static net.yudichev.jiotty.common.testutil.AssertionArgumentMatcher.assertArg;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LivenessCheckerTest {

    private static final Duration DATA_MISSING_THRESHOLD = Duration.ofMinutes(10);
    private ProgrammableClock clock;
    private LivenessChecker checker;
    @Mock
    private Consumer<Collection<TimestampedData>> errorHandler;
    @Mock
    private BiConsumer<TimestampedData, TimestampedData> recoveryHandler;

    @BeforeEach
    void setUp() {
        clock = new ProgrammableClock();
        checker = new LivenessChecker(clock.createSingleThreadedSchedulingExecutor("threadName"),
                                      clock,
                                      DATA_MISSING_THRESHOLD,
                                      errorHandler,
                                      recoveryHandler);
    }

    @Test
    void scenario() {
        clock.advanceTimeAndTick(Duration.ofMinutes(1));
        checker.accept("1");
        clock.advanceTimeAndTick(Duration.ofMinutes(1));
        checker.accept("2");
        clock.advanceTimeAndTick(Duration.ofMinutes(2));
        checker.accept("3");

        // 2x beyond
        clock.advanceTimeAndTick(DATA_MISSING_THRESHOLD.multipliedBy(2));
        verify(errorHandler).accept(assertArg(instants -> assertThat(instants,
                                                                     containsInAnyOrder(TimestampedData.of(EPOCH.plus(1, MINUTES), "1"),
                                                                                        TimestampedData.of(EPOCH.plus(2, MINUTES), "2"),
                                                                                        TimestampedData.of(EPOCH.plus(4, MINUTES), "3")))));
        // recovery
        clock.advanceTimeAndTick(Duration.ofMinutes(1));
        checker.accept("4");
        verify(recoveryHandler).accept(TimestampedData.of(EPOCH.plus(4, MINUTES), "3"),
                                       TimestampedData.of(EPOCH.plus(5, MINUTES).plus(DATA_MISSING_THRESHOLD.multipliedBy(2)), "4"));
    }
}