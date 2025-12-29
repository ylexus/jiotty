package net.yudichev.jiotty.connector.tesla.fleet;

import net.yudichev.jiotty.common.lang.Json;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TelemetryConfigTest {
    @Test
    void deserializesWithUnknownFields() {
        String json = """
                      {
                        "ca": "my-ca",
                        "hostname": "my-host",
                        "port": 123,
                        "fields": {
                          "DetailedChargeState": {
                            "interval_seconds": 1800
                          }
                        }
                      }
                      """;
        TelemetryConfig config = Json.parse(json, TelemetryConfig.class);
        assertThat(config.fieldParams()).containsExactlyInAnyOrderEntriesOf(
                Map.of(TelemetryField.TDetailedChargeState.NAME, TelemetryFieldParams.builder().setIntervalSeconds(1800).build()));
        assertThat(config.caCertificate()).isEqualTo("my-ca");
        assertThat(config.hostname()).isEqualTo("my-host");
        assertThat(config.port()).isEqualTo(123);
    }
}
