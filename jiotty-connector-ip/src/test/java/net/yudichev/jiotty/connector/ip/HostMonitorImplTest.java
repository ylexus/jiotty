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
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static java.time.temporal.ChronoUnit.SECONDS;
import static net.yudichev.jiotty.common.lang.MoreThrowables.asUnchecked;
import static net.yudichev.jiotty.connector.ip.HostMonitor.Status.DOWN;
import static net.yudichev.jiotty.connector.ip.HostMonitor.Status.UP;
import static net.yudichev.jiotty.connector.ip.HostMonitorImpl.InetAddressResolver;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
        executor = clock.createSingleThreadedSchedulingExecutor("test");
    }

    @Test
    void initialPingFailingOnlyNotifiedAfterStabilisationPeriod() {
        createMonitor("hostname");

        expectFailure("hostname");
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
        createMonitor("hostname");

        expectSuccessfulPing("hostname");
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
        createMonitor("hostname");

        // setup with initial ping succeeding
        expectSuccessfulPing("hostname");
        timeIs(0);
        monitor.start();
        monitor.addListener(statusConsumer, directExecutor());
        clock.advanceTimeAndTick(Duration.ofSeconds(30));
        verify(statusConsumer).accept(UP);

        clock.advanceTimeAndTick(Duration.ofSeconds(30));

        executor.schedule(Duration.ofSeconds(10), () -> expectFailure("hostname"));
        clock.advanceTimeAndTick(Duration.ofSeconds(10));
        executor.schedule(Duration.ofSeconds(10), () -> expectSuccessfulPing("hostname"));

        clock.advanceTimeAndTick(Duration.ofSeconds(60));
        verifyNoMoreInteractions(statusConsumer);
    }

    @SuppressWarnings({"unused", "BoundedWildcard", "OverlyLongMethod"})
    @ParameterizedTest(name = "{0}")
    @MethodSource
    void hostGoingDown_ConsumerCalledAfterStabilisationPeriod(String name, Consumer<InetAddressResolver> pingFailure) {
        createMonitor("hostname");

        // setup with initial ping succeeding
        expectSuccessfulPing("hostname");
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
        executor.schedule(Duration.ofSeconds(25), () -> expectSuccessfulPing("hostname"));
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
        executor.schedule(Duration.ofSeconds(1), () -> expectSuccessfulPing("hostname"));
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
        executor.schedule(Duration.ofSeconds(15), () -> expectSuccessfulPing("hostname"));
        timeIs(230);
        verify(statusConsumer, never()).accept(any());

        // 300s: notification
        timeIs(300);
        verify(statusConsumer).accept(UP);

        monitor.stop();
        verifyNoMoreInteractions(statusConsumer);
    }

    @SuppressWarnings("CodeBlock2Expr") // reads better
    static Stream<Arguments> hostGoingDown_ConsumerCalledAfterStabilisationPeriod() {
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

    @Test
    void shutdownWhileInFlight() {
        createMonitor("hostname");
        expectSuccessfulPing("hostname");
        timeIs(0);
        monitor.start();
        monitor.addListener(statusConsumer, directExecutor());
        clock.tick();

        clock.advanceTimeAndTick(Duration.ofSeconds(29).plus(Duration.ofMillis(500)));
        monitor.stop();
        clock.advanceTimeAndTick(Duration.ofSeconds(1));
    }

    @Test
    void multipleHosts() {
        createMonitor("host1", "host2");

        // setup with initial ping succeeding
        expectSuccessfulPing("host1");
        monitor.start();
        monitor.addListener(statusConsumer, directExecutor());
        clock.advanceTimeAndTick(Duration.ofSeconds(60));
        verify(statusConsumer).accept(UP);

        expectSuccessfulPing("host2");
        expectFailure("host1");
        clock.advanceTimeAndTick(Duration.ofSeconds(60));
        verifyNoMoreInteractions(statusConsumer);

        reset(inetAddressResolver, statusConsumer);
        expectFailure("host1");
        expectFailure("host2");
        clock.advanceTimeAndTick(Duration.ofSeconds(60));
        verify(statusConsumer).accept(DOWN);

        reset(statusConsumer);
        expectSuccessfulPing("host2");
        expectFailure("host1");
        clock.advanceTimeAndTick(Duration.ofSeconds(60));
        verify(statusConsumer).accept(UP);
    }

    private void createMonitor(String... hostnames) {
        monitor = new HostMonitorImpl(clock,
                clock,
                List.of(hostnames),
                "deviceName",
                Duration.ofSeconds(30),
                inetAddressResolver);
    }

    private void expectSuccessfulPing(String hostname) {
        asUnchecked(() -> {
            reset(inetAddressResolver, inetAddress);
            lenient().when(inetAddressResolver.resolve(hostname)).thenReturn(inetAddress);
            lenient().when(inetAddress.isReachable(anyInt())).thenReturn(true);
        });
    }

    private void expectFailure(String hostname) {
        asUnchecked(() -> lenient().when(inetAddressResolver.resolve(hostname)).thenThrow(new UnknownHostException(hostname)));
    }

    private void timeIs(long seconds) {
        clock.setTimeAndTick(Instant.EPOCH.plus(seconds, SECONDS));
    }
}