package net.yudichev.jiotty.connector.homeassistant;

import com.google.common.reflect.TypeToken;
import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.Json;
import net.yudichev.jiotty.common.rest.ContentTypes;
import net.yudichev.jiotty.common.rest.RestClients;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.yudichev.jiotty.common.lang.Closeable.closeSafelyIfNotNull;

public final class HomeAssistantClientImpl extends BaseLifecycleComponent implements HomeAssistantClient {
    private static final Logger logger = LoggerFactory.getLogger(HomeAssistantClientImpl.class);

    private final String baseUrl;
    private final String accessToken;
    private final Climate climate = new ClimateImpl();
    private final AtomicLong requestIdGenerator = new AtomicLong();

    private OkHttpClient client;

    @Inject
    public HomeAssistantClientImpl(@BaseUrl String baseUrl, @AccessToken String accessToken) {
        this.baseUrl = checkNotNull(baseUrl);
        this.accessToken = checkNotNull(accessToken);
    }

    @Override
    protected void doStart() {
        client = RestClients.newClient();
    }

    @Override
    protected void doStop() {
        closeSafelyIfNotNull(logger, () -> RestClients.shutdown(client));
    }

    @Override
    public Climate climate() {
        return climate;
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface BaseUrl {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface AccessToken {
    }

    public class ClimateImpl implements Climate {

        @Override
        public CompletableFuture<List<HAState>> setTemperature(String entityId, String hvacMode, double temperature) {
            long requestId = requestIdGenerator.incrementAndGet();
            logger.debug("[{}] Invoking POST setTemperature({}, {}, {})", requestId, entityId, hvacMode, temperature);
            return invokePost(requestId, "set_temperature", HAClimateSetTemperatureServiceData.builder()
                                                                                              .setEntityId(entityId)
                                                                                              .setHvacMode(hvacMode)
                                                                                              .setTemperature(temperature)
                                                                                              .build());
        }

        @Override
        public CompletableFuture<List<HAState>> turnOn(String entityId) {
            long requestId = requestIdGenerator.incrementAndGet();
            logger.debug("[{}] invoking POST turnOn({})", requestId, entityId);
            return invokePost(requestId, "turn_on", HAServiceData.of(entityId));
        }

        @Override
        public CompletableFuture<List<HAState>> turnOff(String entityId) {
            long requestId = requestIdGenerator.incrementAndGet();
            logger.debug("[{}] invoking POST turnOff({})", requestId, entityId);
            return invokePost(requestId, "turn_off", HAServiceData.of(entityId));
        }

        private CompletableFuture<List<HAState>> invokePost(long requestId, String service, Object body) {
            var request = new Request.Builder()
                    .url(baseUrl + "/services/climate/" + service)
                    .header("Authorization", "Bearer " + accessToken)
                    .post(RequestBody.create(Json.stringify(body), MediaType.get(ContentTypes.CONTENT_TYPE_JSON)))
                    .addHeader(CONTENT_TYPE, ContentTypes.CONTENT_TYPE_JSON)
                    .build();
            return callAndLogResponse(request, requestId);
        }

        private CompletableFuture<List<HAState>> callAndLogResponse(Request request, long requestId) {
            return whenStartedAndNotLifecycling(
                    () -> RestClients.<List<HAState>>call(request, new TypeToken<>() {})
                                     .whenComplete((o, throwable) -> logger.debug("[{}] Response {}", requestId, o, throwable)));
        }
    }
}
