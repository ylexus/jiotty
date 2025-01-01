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
import java.net.URLEncoder;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.yudichev.jiotty.common.lang.Closeable.closeSafelyIfNotNull;

public final class HomeAssistantClientImpl extends BaseLifecycleComponent implements HomeAssistantClient {
    private static final Logger logger = LoggerFactory.getLogger(HomeAssistantClientImpl.class);

    private final String baseUrl;
    private final String accessToken;
    private final Climate climate = new ClimateImpl();
    private final Switch theSwitch = new SwitchImpl();
    private final Number number = new NumberImpl();
    private final Button button = new ButtonImpl();
    private final Domain sensor = new BaseDomain("sensor");
    private final LogBook logBook = new LogBookImpl();
    private final BinarySensor binarySensor = new BinarySensorImpl();
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

    @Override
    public Switch aSwitch() {
        return theSwitch;
    }

    @Override
    public Number number() {
        return number;
    }

    @Override
    public Button button() {
        return button;
    }

    @Override
    public Domain sensor() {
        return sensor;
    }

    @Override
    public LogBook logBook() {
        return logBook;
    }

    @Override
    public BinarySensor binarySensor() {
        return binarySensor;
    }

    private <T> CompletableFuture<T> invokeGet(String path, TypeToken<T> responseType) {
        long requestId = requestIdGenerator.incrementAndGet();
        logger.debug("[{}] Invoking GET {}", requestId, path);
        var request = new Request.Builder().url(baseUrl + '/' + path)
                                           .header("Authorization", "Bearer " + accessToken)
                                           .get()
                                           .build();
        return callAndLogResponse(request, requestId, responseType);
    }

    private <T> CompletableFuture<T> callAndLogResponse(Request request, long requestId, TypeToken<T> responseType) {
        return whenStartedAndNotLifecycling(
                () -> RestClients.<T>call(request, responseType)
                                 .whenComplete((o, throwable) -> logger.debug("[{}] Response {}", requestId, o, throwable)));
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

    private class BaseDomain implements Domain {
        protected final String domainId;

        protected BaseDomain(String domainId) {
            this.domainId = checkNotNull(domainId);
        }

        @Override
        public CompletableFuture<HAState> getState(String domainlessEntityId) {
            return invokeGetState(domainlessEntityId);
        }

        protected CompletableFuture<List<HAState>> invokePostServices(String service, Object body) {
            long requestId = requestIdGenerator.incrementAndGet();
            logger.debug("[{}] Invoking POST {}.{}({})", requestId, domainId, service, body);
            var request = new Request.Builder().url(baseUrl + "/services/" + domainId + "/" + service)
                                               .header("Authorization", "Bearer " + accessToken)
                                               .addHeader(CONTENT_TYPE, ContentTypes.CONTENT_TYPE_JSON)
                                               .post(RequestBody.create(Json.stringify(body), MediaType.get(ContentTypes.CONTENT_TYPE_JSON)))
                                               .build();
            return callAndLogResponse(request, requestId, new TypeToken<>() {});
        }

        protected CompletableFuture<HAState> invokeGetState(String domainlessEntityId) {
            return invokeGet("states/" + domainId + '.' + domainlessEntityId, new TypeToken<>() {});
        }
    }

    private class ClimateImpl extends BaseDomain implements Climate {
        public ClimateImpl() {
            super("climate");
        }

        @Override
        public CompletableFuture<List<HAState>> setTemperature(String entityId, String hvacMode, double temperature) {
            return invokePostServices("set_temperature", HAClimateSetTemperatureServiceData.builder()
                                                                                           .setEntityId(entityId)
                                                                                           .setHvacMode(hvacMode)
                                                                                           .setTemperature(temperature)
                                                                                           .build());
        }

        @Override
        public CompletableFuture<List<HAState>> turnOn(String entityId) {
            return invokePostServices("turn_on", HAServiceData.of(entityId));
        }

        @Override
        public CompletableFuture<List<HAState>> turnOff(String entityId) {
            return invokePostServices("turn_off", HAServiceData.of(entityId));
        }
    }

    private class SwitchImpl extends BaseDomain implements Switch {
        public SwitchImpl() {
            super("switch");
        }

        @Override
        public CompletableFuture<List<HAState>> turnOn(String entityId) {
            return invokePostServices("turn_on", HAServiceData.of(entityId));
        }

        @Override
        public CompletableFuture<List<HAState>> turnOff(String entityId) {
            return invokePostServices("turn_off", HAServiceData.of(entityId));
        }
    }

    private class NumberImpl extends BaseDomain implements Number {
        public NumberImpl() {
            super("number");
        }

        @Override
        public CompletableFuture<List<HAState>> setValue(String entityId, double value) {
            return invokePostServices("set_value", HANumberSetValueServiceData.of(entityId, value));
        }
    }

    private class ButtonImpl extends BaseDomain implements Button {
        public ButtonImpl() {
            super("button");
        }

        @Override
        public CompletableFuture<List<HAState>> press(String entityId) {
            return invokePostServices("press", HAServiceData.of(entityId));
        }
    }

    private class BinarySensorImpl extends BaseDomain implements BinarySensor {
        public BinarySensorImpl() {
            super("binary_sensor");
        }
    }

    private class LogBookImpl implements LogBook {

        @Override
        public CompletableFuture<List<HALogbookEntry>> get(String entityId, Optional<Instant> from, Optional<Instant> to) {
            return invokeGet("logbook" + from.map(t -> "/" + t).orElse("")
                                     + "?entity=" + URLEncoder.encode(entityId, UTF_8)
                                     + to.map(t -> "&end_time=" + t).orElse(""),
                             new TypeToken<>() {});
        }
    }
}
