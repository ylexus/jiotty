package net.yudichev.jiotty.connector.shelly;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static net.yudichev.jiotty.connector.shelly.SwitchEnergyStatus.of;
import static org.assertj.core.api.Assertions.assertThat;


class ShellyPlugImplTest {
    @Test
    void sampleAggregator() {
        var a = new ShellyPlugImpl.SampleAggregator();

        assertThat(a.processResponse(of(3 * 60, List.of(3.0, 2.0, 1.0)))).isFalse();
        assertThat(a.processResponse(of(4 * 60, List.of(4.0, 3.0, 2.0)))).isFalse();
        assertThat(a.processResponse(of(9 * 60, List.of(9.0, 8.0, 7.0)))).isFalse(); // missed 1 minute
        assertThat(a.processResponse(of(10 * 60 + 15, List.of(10.0, 9.0, 8.0)))).isFalse(); // misaligned time shifted by 15s
        assertThat(a.processResponse(of(11 * 60, List.of(11.0, 10.0)))).isFalse(); // only 2 elements in array

        /*
           time  -> 1 | | | 2 | | | 3 | | | 4 | | | 5 | | | 6 | | | 7 | | | 8 | | | 9  |  |  |  10  |  |  |  11
           value -> 1       2       3       4                       7       8 8     9  9        10  10       11
         */

        assertThat(a.generateConsumptionCurve()).hasValue(new ShellyPlug.ConsumptionCurve(Instant.ofEpochSecond(60), List.of(
                1.0, 2.0, 3.0, 4.0, 4.0, 7.0, 7.0, 8.0, 9.0, 10.0, 11.0
        )));
    }
}