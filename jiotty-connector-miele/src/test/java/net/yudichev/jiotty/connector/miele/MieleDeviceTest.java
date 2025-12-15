package net.yudichev.jiotty.connector.miele;

import com.google.common.reflect.TypeToken;
import net.yudichev.jiotty.common.lang.Json;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class MieleDeviceTest {
    @Test
    void deserialises() throws IOException {
        try (var resourceAsStream = getClass().getResourceAsStream("/miele-device.json")) {
            assert resourceAsStream != null;
            MieleDevice mieleDevice = Json.parse(new String(resourceAsStream.readAllBytes(), UTF_8), new TypeToken<>() {});
            assertThat(mieleDevice.state()).satisfies(state -> {
                assertThat(state.program()).isEqualTo(StateValue.builder()
                                                                .setId(7)
                                                                .setName("Auto")
                                                                .build());
                assertThat(state.remainingTime()).hasValue(Duration.ofHours(3).plusMinutes(11));
                assertThat(state.status()).isEqualTo(StateValue.builder()
                                                               .setId(5)
                                                               .setName("In use")
                                                               .build());
                assertThat(state.remoteControlStatus()).isEqualTo(RemoteControlStatus.builder()
                                                                                     .setMobileStart(true)
                                                                                     .setFullRemoteControl(true)
                                                                                     .build());
            });
        }
    }
}