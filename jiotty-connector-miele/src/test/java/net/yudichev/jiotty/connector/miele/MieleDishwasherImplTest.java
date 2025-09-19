package net.yudichev.jiotty.connector.miele;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.base.Throwables;
import net.yudichev.jiotty.common.async.ExecutorFactoryImpl;
import net.yudichev.jiotty.common.async.backoff.RetryableOperationExecutor;
import net.yudichev.jiotty.common.lang.Closeable;
import net.yudichev.jiotty.common.security.OAuth2TokenManager;
import net.yudichev.jiotty.common.time.TimeProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

/**
 * Originally from <a href="https://chatgpt.com/c/6754cbf0-1374-800b-9e27-9ff9d91b33f0">here</a>
 */
@WireMockTest
class MieleDishwasherImplTest {
    private static final Logger logger = LoggerFactory.getLogger(MieleDishwasherImplTest.class);

    private static final String DEVICE_ID = "test-device-id";
    private static final String ACCESS_TOKEN = "mock-access-token";

    private MieleDishwasherImpl mieleDishwasher;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        String baseUrl = wmRuntimeInfo.getHttpBaseUrl();
        OAuth2TokenManager mockTokenManager = accessTokenHandler -> {
            accessTokenHandler.accept(ACCESS_TOKEN);
            return () -> {};
        };

        mieleDishwasher = new MieleDishwasherImpl(
                DEVICE_ID,
                mockTokenManager,
                new ExecutorFactoryImpl(),
                new TimeProvider(),
                RetryableOperationExecutor.noRetries(),
                baseUrl,
                builder -> builder.connectTimeout(Duration.ofSeconds(1))
                                  .callTimeout(Duration.ofSeconds(1))
                                  .readTimeout(Duration.ofSeconds(1))
                                  .writeTimeout(Duration.ofSeconds(1)),
                builder -> {},
                1);
        mieleDishwasher.start();
    }

    @AfterEach
    void tearDown() {
        Closeable.closeSafelyIfNotNull(logger, () -> mieleDishwasher.stop());
    }

    @Test
    void getPrograms_successfulResponse_returnsPrograms() {
        stubFor(get(urlPathEqualTo("/devices/" + DEVICE_ID + "/programs"))
                        .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody("""
                                                      [
                                                          {"programId": 1, "program": "Program 1"},
                                                          {"programId": 2, "program": "Program 2"}
                                                      ]
                                                      """)));

        List<MieleProgram> programs = mieleDishwasher.getPrograms().join();

        assertThat(programs).hasSize(2)
                            .extracting("name")
                            .containsExactly("Program 1", "Program 2");
    }

    @Test
    void getPrograms_timeout_throwsException() {
        stubFor(get(urlPathEqualTo("/devices/" + DEVICE_ID + "/programs"))
                        .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
                        .willReturn(aResponse()
                                            .withFixedDelay(1500) // Delay longer than client timeout
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBody("[]")));

        assertThatThrownBy(() -> mieleDishwasher.getPrograms().join())
                .extracting(Throwables::getCausalChain).asInstanceOf(LIST)
                .anySatisfy(e -> assertThat(e).isInstanceOf(SocketTimeoutException.class));
    }

    @Test
    void getPrograms_serverError_throwsException() {
        stubFor(get(urlPathEqualTo("/devices/" + DEVICE_ID + "/programs"))
                        .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
                        .willReturn(aResponse()
                                            .withStatus(500)
                                            .withBody("Internal Server Error")));

        assertThatThrownBy(() -> mieleDishwasher.getPrograms().join())
                .hasMessageContaining("500");
    }

    @Test
    void startProgram_successful() {
        stubFor(put(urlPathEqualTo("/devices/" + DEVICE_ID + "/programs"))
                        .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
                        .withRequestBody(equalToJson("""
                                                     {"programId": 123}
                                                     """))
                        .willReturn(aResponse()
                                            .withStatus(204)));

        mieleDishwasher.startProgram(123).join();

        verify(putRequestedFor(urlPathEqualTo("/devices/" + DEVICE_ID + "/programs"))
                       .withRequestBody(equalToJson("""
                                                    {"programId": 123}
                                                    """)));
    }

    @Test
    void startProgram_connectionFailure_retriesAndFails() {
        stubFor(put(urlPathEqualTo("/devices/" + DEVICE_ID + "/programs"))
                        .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertThatThrownBy(() -> mieleDishwasher.startProgram(123).join())
                .hasRootCauseInstanceOf(IOException.class);
    }
}
