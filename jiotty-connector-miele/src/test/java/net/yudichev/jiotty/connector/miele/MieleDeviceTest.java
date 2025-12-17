package net.yudichev.jiotty.connector.miele;

import com.google.common.reflect.TypeToken;
import net.yudichev.jiotty.common.lang.Json;
import org.assertj.core.api.ThrowingConsumer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class MieleDeviceTest {
    @Test
    void deserialises1() throws IOException {
        testDeserialisation("/miele-device-1.json", state -> {
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

    @Test
    void deserialises2() throws IOException {
        testDeserialisation("/miele-device-2.json", state -> {
            assertThat(state.program()).isEqualTo(StateValue.builder()
                                                            .setId(0)
                                                            .setName("")
                                                            .build());
            assertThat(state.remainingTime()).isEmpty();
            assertThat(state.status()).isEqualTo(StateValue.builder()
                                                           .setId(255)
                                                           .setName("Not connected")
                                                           .build());
            assertThat(state.remoteControlStatus()).isEqualTo(RemoteControlStatus.builder()
                                                                                 .setMobileStart(false)
                                                                                 .setFullRemoteControl(false)
                                                                                 .build());
        });
    }

    private void testDeserialisation(String name, ThrowingConsumer<DeviceState> deviceStateThrowingConsumer) throws IOException {
        try (var resourceAsStream = getClass().getResourceAsStream(name)) {
            assert resourceAsStream != null;
            MieleDevice mieleDevice = Json.parse(new String(resourceAsStream.readAllBytes(), UTF_8), new TypeToken<>() {});
            assertThat(mieleDevice.state()).satisfies(deviceStateThrowingConsumer);
        }
    }
}