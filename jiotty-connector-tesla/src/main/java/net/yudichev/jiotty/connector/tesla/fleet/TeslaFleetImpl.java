package net.yudichev.jiotty.connector.tesla.fleet;

import com.google.common.reflect.TypeToken;
import com.google.inject.BindingAnnotation;
import jakarta.inject.Inject;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.Closeable;
import net.yudichev.jiotty.common.lang.Json;
import net.yudichev.jiotty.common.net.SslCustomisation;
import net.yudichev.jiotty.common.rest.ContentTypes;
import net.yudichev.jiotty.common.security.OAuth2TokenManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static net.yudichev.jiotty.common.lang.Closeable.closeSafelyIfNotNull;
import static net.yudichev.jiotty.common.lang.MoreThrowables.asUnchecked;
import static net.yudichev.jiotty.common.rest.RestClients.call;
import static net.yudichev.jiotty.common.rest.RestClients.newClient;
import static net.yudichev.jiotty.common.rest.RestClients.shutdown;


public final class TeslaFleetImpl extends BaseLifecycleComponent implements TeslaFleet {
    private static final Logger logger = LoggerFactory.getLogger(TeslaFleetImpl.class);
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final TypeToken<ResponseWrapper<List<TeslaVehicleData>>> LIST_VEHICLES_RESPONSE_TYPE = new TypeToken<>() {};

    private final OAuth2TokenManager tokenManager;
    private final String baseUrl;
    private final AtomicInteger requestIdGenerator = new AtomicInteger();
    private final String listVehiclesUrl;
    private final @Nullable SslCustomisation sslCustomisation;

    private OkHttpClient httpClient;
    @Nullable
    private Closeable tokenSubscription;
    private volatile String accessToken;

    @Inject
    public TeslaFleetImpl(@Dependency OAuth2TokenManager tokenManager,
                          @BaseUrl String baseUrl,
                          @Dependency Optional<SslCustomisation> sslCustomisation) {
        this.tokenManager = checkNotNull(tokenManager);
        this.baseUrl = Objects.requireNonNull(baseUrl);
        this.sslCustomisation = sslCustomisation.orElse(null);
        listVehiclesUrl = baseUrl + "/vehicles";
    }

    @Override
    protected void doStart() {
        httpClient = newClient(builder -> {
            if (sslCustomisation != null) {
                builder.sslSocketFactory(sslCustomisation.socketFactory(), sslCustomisation.trustManager());
            }
        });
        CompletableFuture<Void> firstToken = new CompletableFuture<>();
        tokenSubscription = tokenManager.subscribeToAccessToken(token -> {
            boolean firstTime = accessToken == null;
            accessToken = token;
            if (firstTime) {
                firstToken.complete(null);
            }
        });
        if (!firstToken.isDone()) {
            logger.info("Awaiting for the access token to be delivered...");
            asUnchecked(() -> firstToken.get(5, TimeUnit.MINUTES));
            logger.info("Access token obtained");
        }
    }

    @Override
    protected void doStop() {
        closeSafelyIfNotNull(logger, tokenSubscription, () -> shutdown(httpClient));
    }

    @Override
    public CompletableFuture<List<TeslaVehicleData>> listVehicles() {
        return whenStartedAndNotLifecycling(() -> {
            var request = new Request.Builder()
                    .url(listVehiclesUrl)
                    .header("Authorization", "Bearer " + accessToken)
                    .get()
                    .build();

            int requestId = requestIdGenerator.incrementAndGet();
            logger.debug("[{}] executing {}", requestId, listVehiclesUrl);
            // https://developer.tesla.com/docs/fleet-api/endpoints/vehicle-endpoints#list
            // Ignoring pagination
            return call(httpClient.newCall(request), LIST_VEHICLES_RESPONSE_TYPE, 0)
                    .whenComplete((resp, throwable) -> logger.debug("[{}] result {}", requestId, resp, throwable))
                    .thenApply(ResponseWrapper::response);
        });
    }

    @Override
    public TeslaVehicle vehicle(String vin) {
        return new TeslaVehicleImpl(vin);
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface BaseUrl {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }

    private final class TeslaVehicleImpl implements TeslaVehicle {
        private static final TypeToken<ResponseWrapper<CommandResponse>> CMD_RESPONSE_TYPE = new TypeToken<>() {};
        private static final TypeToken<ResponseWrapper<VehicleData>> GET_VEHICLE_DATA_RESPONSE_TYPE = new TypeToken<>() {};
        private final String getDataUrl;
        private final String wakeUpUrl;
        private final String setChargeLimitUrl;
        private final String chargeStartUrl;
        private final String chargeStopUrl;
        private final String autoConditioningStartUrl;
        private final String autoConditioningStopUrl;

        TeslaVehicleImpl(String vin) {
            getDataUrl = baseUrl + "/vehicles/" + vin + "/vehicle_data";
            wakeUpUrl = baseUrl + "/vehicles/" + vin + "/wake_up";
            setChargeLimitUrl = baseUrl + "/vehicles/" + vin + "/command/set_charge_limit";
            chargeStartUrl = baseUrl + "/vehicles/" + vin + "/command/charge_start";
            chargeStopUrl = baseUrl + "/vehicles/" + vin + "/command/charge_stop";
            autoConditioningStartUrl = baseUrl + "/vehicles/" + vin + "/command/auto_conditioning_start";
            autoConditioningStopUrl = baseUrl + "/vehicles/" + vin + "/command/auto_conditioning_stop";
        }

        @Override
        public CompletableFuture<Optional<VehicleData>> getData(Set<Endpoint> endpoints) {
            /*
            String of URL-encoded, semicolon-separated values. Can be many of 'charge_state', 'climate_state', 'closures_state', 'drive_state',
             'gui_settings', 'location_data', 'charge_schedule_data', 'preconditioning_schedule_data', 'vehicle_config', 'vehicle_state',
              'vehicle_data_combo'. The 'location_data' and 'location_state' endpoints require 'vehicle_location' scope
             */
            // GET vehicle_data
            String url = getDataUrl + "?endpoints=" + URLEncoder.encode(endpoints.stream().map(Endpoint::id).collect(joining(";")), UTF_8);
            var request = new Request.Builder().url(url)
                                               .header("Authorization", "Bearer " + accessToken)
                                               .get()
                                               .build();
            int requestId = requestIdGenerator.incrementAndGet();
            logger.debug("[{}] executing {}", requestId, url);
            return callAndAllow408(requestId, httpClient.newCall(request), GET_VEHICLE_DATA_RESPONSE_TYPE)
                    .whenComplete((resp, throwable) -> logger.debug("[{}] result {}", requestId, resp, throwable))
                    .thenApply(wrapperOptional -> wrapperOptional.map(ResponseWrapper::response));
        }

        private static <T> CompletableFuture<Optional<T>> callAndAllow408(int requestId, Call theCall, TypeToken<? extends T> responseType) {
            CompletableFuture<Optional<T>> future = new CompletableFuture<>();
            theCall.enqueue(new Callback() {

                private static final int RESPONSE_CODE_OFFLINE = 408;

                @Override
                public void onFailure(Call call, IOException e) {
                    future.completeExceptionally(new RuntimeException("call failed: " + call.request(), e));
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try (ResponseBody responseBody = response.body()) {
                        try {
                            if (response.isSuccessful()) {
                                String responseString = requireNonNull(responseBody).string();
                                logger.debug("[{}] response: {}", requestId, responseString);
                                T responseData;
                                try {
                                    responseData = Json.parse(responseString, responseType);
                                    future.complete(Optional.of(responseData));
                                } catch (RuntimeException e) {
                                    future.completeExceptionally(new RuntimeException("Failed parsing response " + responseString, e));
                                }
                            } else if (response.code() == RESPONSE_CODE_OFFLINE) {
                                future.complete(Optional.empty());
                            } else {
                                future.completeExceptionally(new RuntimeException(
                                        "Response code " + response.code() + (responseBody == null ? "" : ", body: " + responseBody.string())));
                            }
                        } catch (RuntimeException | IOException e) {
                            future.completeExceptionally(new RuntimeException("failed to process response body", e));
                        }
                    }
                }
            });
            return future;
        }

        @Override
        public CompletableFuture<Void> wakeUp() {
            return post(wakeUpUrl, null, new TypeToken<ResponseWrapper<WakeUpResult>>() {})
                    .<Void>thenApply(ignored -> null);
        }

        @Override
        public CompletableFuture<Void> setChargeLimit(int limitPercent) {
            String json = "{\"percent\":" + limitPercent + "}";
            return postCommand(setChargeLimitUrl, json);
        }

        @Override
        public CompletableFuture<Void> startCharge() {
            return postCommand(chargeStartUrl, null);
        }

        @Override
        public CompletableFuture<Void> stopCharge() {
            return postCommand(chargeStopUrl, null);
        }

        @Override
        public CompletableFuture<Void> startAutoConditioning() {
            return postCommand(autoConditioningStartUrl, null);
        }

        @Override
        public CompletableFuture<Void> stopAutoConditioning() {
            return postCommand(autoConditioningStopUrl, null);
        }

        private CompletableFuture<Void> postCommand(String url, @Nullable String jsonBody) {
            return post(url, jsonBody, CMD_RESPONSE_TYPE)
                    .thenApply(responseWrapper -> {
                        if (responseWrapper.response().result()) {
                            return null;
                        } else {
                            throw new RuntimeException(responseWrapper.response().reason().orElse("unknown reason"));
                        }
                    });
        }

        private <T> CompletableFuture<ResponseWrapper<T>> post(String url,
                                                               @Nullable String jsonBody,
                                                               TypeToken<ResponseWrapper<T>> responseType) {
            Request.Builder builder = new Request.Builder().url(url)
                                                           .header("Authorization", "Bearer " + accessToken);
            if (jsonBody == null) {
                builder.post(RequestBody.create(EMPTY_BYTE_ARRAY, MediaType.get(ContentTypes.CONTENT_TYPE_JSON)));
            } else {
                builder.post(RequestBody.create(jsonBody, MediaType.get(ContentTypes.CONTENT_TYPE_JSON)));
            }
            Request request = builder.build();
            int requestId = requestIdGenerator.incrementAndGet();
            logger.debug("[{}] executing {}{}", requestId, url, jsonBody == null ? "" : jsonBody);
            return call(httpClient.newCall(request), responseType, 0)
                    .whenComplete((resp, throwable) -> logger.debug("[{}] result {}", requestId, resp, throwable));
        }
    }
}
