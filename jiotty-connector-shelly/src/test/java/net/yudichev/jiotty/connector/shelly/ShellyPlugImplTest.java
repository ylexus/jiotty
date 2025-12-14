package net.yudichev.jiotty.connector.shelly;

import net.yudichev.jiotty.common.async.ProgrammableClock;
import net.yudichev.jiotty.common.async.backoff.RetryableOperationExecutor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static net.yudichev.jiotty.connector.shelly.SwitchEnergyStatus.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShellyPlugImplTest {

    private ProgrammableClock clock;
    private ShellyPlugImpl plug;
    @Mock
    private OkHttpClient httpClient;

    @BeforeEach
    void setUp() {
        clock = new ProgrammableClock();
        plug = new ShellyPlugImpl("host", clock, RetryableOperationExecutor.NO_RETRIES, clock) {
            @Override
            OkHttpClient createHttpClient() {
                return httpClient;
            }
        };
    }

    @Test
    void sampleAggregator() {
        var a = plug.new SampleAggregator();

        assertThat(a.processResponse(of(3 * 60, List.of(3.0, 2.0, 1.0)))).isFalse();
        assertThat(a.processResponse(of(4 * 60, List.of(4.0, 3.0, 2.0)))).isFalse();
        assertThat(a.processResponse(of(9 * 60, List.of(9.0, 8.0, 7.0)))).isFalse(); // missed 1 minute
        assertThat(a.processResponse(of(10 * 60 + 15, List.of(10.0, 9.0, 8.0)))).isFalse(); // misaligned time shifted by 15s
        assertThat(a.processResponse(of(11 * 60, List.of(11.0, 10.0)))).isFalse(); // only 2 elements in array

        /*
           time  -> 0 | | | 1 | | | 2 | | | 3 | | | 4 | | | 5 | | | 6 | | | 7 | | | 8 | | | 9 | | | 10 | | | 11
           value -> 1       2       3       4                       7       8 8     9  9        10  10       11
         */

        assertThat(a.generateConsumptionCurve()).hasValue(new ShellyPlug.ConsumptionCurve(Instant.ofEpochSecond(0), List.of(
                1.0, 2.0, 3.0, 4.0, 4.0, 7.0, 7.0, 8.0, 9.0, 10.0, 11.0
        )));
    }

    @Test
    void startAfterFailure(@Mock Consumer<String> errorHandler, @Mock Call call) {
        when(httpClient.newCall(any())).thenReturn(call);
        AtomicInteger counter = new AtomicInteger();
        when(call.request()).thenReturn(new okhttp3.Request.Builder().method("GET", null).url("http://host:1234").build());
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(0);
            callback.onFailure(call, new IOException("no route to host " + counter.incrementAndGet()));
            return null;
        }).when(call).enqueue(any());
        plug.start();
        clock.tick();

        var consumptionMeasurement = plug.startMeasuringConsumption(errorHandler);
        clock.tick();
        verify(httpClient).newCall(any());
        verify(errorHandler).accept(contains("no route to host 1"));
        assertThatThrownBy(consumptionMeasurement::stop).hasMessageContaining("already stopped or failed");

        reset(errorHandler);
        plug.startMeasuringConsumption(errorHandler);
        clock.tick();
        verify(httpClient, times(2)).newCall(any());
        verify(errorHandler).accept(contains("no route to host 2"));
        assertThatThrownBy(consumptionMeasurement::stop).hasMessageContaining("already stopped or failed");
    }
}