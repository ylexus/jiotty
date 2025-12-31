package net.yudichev.jiotty.connector.tesla.fleet;

import com.google.common.reflect.TypeToken;
import net.yudichev.jiotty.common.lang.Json;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TelemetryCreateConfigResponseTest {
    @Test
    void parses() {
        @Language("JSON") var responseJson =
                """
                {"response":{"updated_vehicles":1}}
                """;
        assertThat(Json.parse(responseJson, new TypeToken<ResponseWrapper<TelemetryCreateConfigResponse>>() {})).
                isEqualTo(ResponseWrapper.builder()
                                         .setResponse(TelemetryCreateConfigResponse.builder()
                                                                                   .setUpdatedVehicles(1)
                                                                                   .build())
                                         .build());
    }
}