package net.yudichev.jiotty.connector.ip;

import net.yudichev.jiotty.common.async.ProgrammableClock;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static java.time.temporal.ChronoUnit.SECONDS;
import static net.yudichev.jiotty.common.lang.MoreThrowables.asUnchecked;
import static net.yudichev.jiotty.connector.ip.HostMonitor.Status.DOWN;
import static net.yudichev.jiotty.connector.ip.HostMonitor.Status.UP;
import static net.yudichev.jiotty.connector.ip.HostMonitorImpl.InetAddressResolver;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class HostMonitorImplTest {

    private ProgrammableClock clock;
    @Mock
    private InetAddressResolver inetAddressResolver;
    @Mock
    private Consumer<HostMonitor.Status> statusConsumer;
    @Mock
    private InetAddress inetAddress;
    private HostMonitorImpl monitor;
    private SchedulingExecutor executor;

    @BeforeEach
    void setUp() {
        clock = new ProgrammableClock().withMdc();
        monitor = new HostMonitorImpl(clock,
                clock,
                "hostname",
                "name",
                Duration.ofSeconds(30),
                inetAddressResolver);
        executor = clock.createSingleThreadedSchedulingExecutor("test");
    }

    @Test
    void initialPingFailingOnlyNotifiedAfterStabilisationPeriod() {
        expectFaiure();
        timeIs(0);
        monitor.start();
        monitor.addListener(statusConsumer, directExecutor());
        clock.tick();
        verify(statusConsumer, never()).accept(any());

        clock.advanceTimeAndTick(Duration.ofSeconds(15));
        verify(statusConsumer, never()).accept(any());

        clock.advanceTimeAndTick(Duration.ofSeconds(30));
        verify(statusConsumer).accept(DOWN);
    }

    @Test
    void initialPingSucceedingOnlyNotifiedAfterStabilisationPeriod() {
        expectSuccessfulPing();
        timeIs(0);
        monitor.start();
        monitor.addListener(statusConsumer, directExecutor());
        clock.tick();
        verify(statusConsumer, never()).accept(any());

        clock.advanceTimeAndTick(Duration.ofSeconds(15));
        verify(statusConsumer, never()).accept(any());

        clock.advanceTimeAndTick(Duration.ofSeconds(30));
        verify(statusConsumer).accept(UP);
    }

    @Test
    void shortGlitchIsNotReported() {
        // setup with initial ping succeeding
        expectSuccessfulPing();
        timeIs(0);
        monitor.start();
        monitor.addListener(statusConsumer, directExecutor());
        clock.advanceTimeAndTick(Duration.ofSeconds(30));
        verify(statusConsumer).accept(UP);

        clock.advanceTimeAndTick(Duration.ofSeconds(30));

        executor.schedule(Duration.ofSeconds(10), this::expectFaiure);
        clock.advanceTimeAndTick(Duration.ofSeconds(10));
        executor.schedule(Duration.ofSeconds(10), this::expectSuccessfulPing);

        clock.advanceTimeAndTick(Duration.ofSeconds(60));
        verifyNoMoreInteractions(statusConsumer);
    }

    private void expectFaiure() {
        asUnchecked(() -> when(inetAddressResolver.resolve("hostname")).thenThrow(new UnknownHostException("hostname")));
    }

    @SuppressWarnings({"unused", "BoundedWildcard", "OverlyLongMethod"})
    @ParameterizedTest(name = "{0}")
    @MethodSource
    void hostGoingDown_ConsumerCalledAfterStabilisationPeriod(String name, Consumer<InetAddressResolver> pingFailure) {
        // setup with initial ping succeeding
        expectSuccessfulPing();
        timeIs(0);
        monitor.start();
        monitor.addListener(statusConsumer, directExecutor());
        clock.tick();
        verify(statusConsumer, never()).accept(any());

        // 10s: host goes down
        executor.schedule(Duration.ofSeconds(10), () -> {
            reset(inetAddressResolver);
            pingFailure.accept(inetAddressResolver);
        });
        timeIs(10);
        verify(statusConsumer, never()).accept(any());

        // 35s: host goes up 25s after going down
        executor.schedule(Duration.ofSeconds(25), this::expectSuccessfulPing);
        timeIs(35);
        verify(statusConsumer, never()).accept(any());

        // 100s: host still up, way past threshold, now stable UP
        timeIs(100);
        verify(statusConsumer).accept(UP);
        reset(statusConsumer);

        // 105s: host goes down again, this time for good
        executor.schedule(Duration.ofSeconds(5), () -> {
            reset(inetAddressResolver);
            pingFailure.accept(inetAddressResolver);
        });
        timeIs(105);
        verify(statusConsumer, never()).accept(any());

        // 135s: past threshold, should notify DOWN
        timeIs(135);
        verify(statusConsumer).accept(DOWN);
        reset(statusConsumer);

        // 200s: nothing changed
        timeIs(200);
        verify(statusConsumer, never()).accept(any());

        // 201s: host goes up
        executor.schedule(Duration.ofSeconds(1), this::expectSuccessfulPing);
        timeIs(201);
        verify(statusConsumer, never()).accept(any());

        // 215s: host goes down
        executor.schedule(Duration.ofSeconds(14), () -> {
            reset(inetAddressResolver);
            pingFailure.accept(inetAddressResolver);
        });
        timeIs(215);
        verify(statusConsumer, never()).accept(any());

        // 230s: host goes up
        executor.schedule(Duration.ofSeconds(15), this::expectSuccessfulPing);
        timeIs(230);
        verify(statusConsumer, never()).accept(any());

        // 300s: notification
        timeIs(300);
        verify(statusConsumer).accept(UP);

        monitor.stop();
        verifyNoMoreInteractions(statusConsumer);
    }

    @SuppressWarnings("CodeBlock2Expr") // reads better
    private static Stream<Arguments> hostGoingDown_ConsumerCalledAfterStabilisationPeriod() {
        return Stream.of(
                Arguments.of("unreachable", (Consumer<InetAddressResolver>) inetAddressResolver -> asUnchecked(() -> {
                    InetAddress inetAddress = mock(InetAddress.class);
                    lenient().when(inetAddressResolver.resolve("hostname")).thenReturn(inetAddress);
                    lenient().when(inetAddress.isReachable(anyInt())).thenReturn(false);
                })),
                Arguments.of("unknown host", (Consumer<InetAddressResolver>) inetAddressResolver -> asUnchecked(() -> {
                    lenient().when(inetAddressResolver.resolve("hostname")).thenThrow(new UnknownHostException("hostname"));
                })),
                Arguments.of("network error", (Consumer<InetAddressResolver>) inetAddressResolver -> asUnchecked(() -> {
                    InetAddress inetAddress = mock(InetAddress.class);
                    lenient().when(inetAddressResolver.resolve("hostname")).thenReturn(inetAddress);
                    lenient().when(inetAddress.isReachable(anyInt())).thenThrow(new IOException("Network error"));
                }))
        );
    }

    private void expectSuccessfulPing() {
        asUnchecked(() -> {
            reset(inetAddressResolver, inetAddress);
            when(inetAddressResolver.resolve("hostname")).thenReturn(inetAddress);
            when(inetAddress.isReachable(anyInt())).thenReturn(true);
        });
    }

    private void timeIs(long seconds) {
        clock.setTimeAndTick(Instant.EPOCH.plus(seconds, SECONDS));
    }
}