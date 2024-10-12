package net.yudichev.jiotty.connector.miele;

import com.google.common.reflect.TypeToken;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import net.yudichev.jiotty.common.async.ExecutorFactory;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.async.backoff.RetryableOperationExecutor;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.BaseIdempotentCloseable;
import net.yudichev.jiotty.common.lang.Closeable;
import net.yudichev.jiotty.common.lang.Json;
import net.yudichev.jiotty.common.lang.Listeners;
import net.yudichev.jiotty.common.lang.backoff.BackOff;
import net.yudichev.jiotty.common.lang.backoff.ExponentialBackOff;
import net.yudichev.jiotty.common.rest.ContentTypes;
import net.yudichev.jiotty.common.time.CurrentDateTimeProvider;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.time.Duration.ZERO;
import static java.time.Duration.ofMillis;
import static net.yudichev.jiotty.common.lang.HumanReadableExceptionMessage.humanReadableMessage;
import static net.yudichev.jiotty.common.lang.MoreThrowables.asUnchecked;
import static net.yudichev.jiotty.common.rest.RestClients.call;
import static net.yudichev.jiotty.common.rest.RestClients.newClient;

final class MieleDishwasherImpl extends BaseLifecycleComponent implements MieleDishwasher {
    private static final String BASE_URL = "https://api.mcs3.miele.com/v1";
    private static final Logger logger = LoggerFactory.getLogger(MieleDishwasherImpl.class);

    private final OkHttpClient eventingClient = newClient(builder -> builder.readTimeout(ZERO).callTimeout(ZERO).writeTimeout(ZERO));
    private final OkHttpClient commandClient = newClient();
    private final String deviceId;
    private final OAuth2TokenManager tokenManager;
    private final ExecutorFactory executorFactory;
    private final CurrentDateTimeProvider dateTimeProvider;
    private final RetryableOperationExecutor retryableOperationExecutor;

    private volatile String accessToken;
    private SchedulingExecutor executor;
    private Closeable tokenSubscription;

    @Inject
    MieleDishwasherImpl(@DeviceId String deviceId,
                        @Dependency OAuth2TokenManager tokenManager,
                        ExecutorFactory executorFactory,
                        CurrentDateTimeProvider dateTimeProvider,
                        @Dependency RetryableOperationExecutor retryableOperationExecutor) {
        this.deviceId = checkNotNull(deviceId);
        this.tokenManager = checkNotNull(tokenManager);
        this.executorFactory = checkNotNull(executorFactory);
        this.dateTimeProvider = checkNotNull(dateTimeProvider);
        this.retryableOperationExecutor = checkNotNull(retryableOperationExecutor);
    }

    @Override
    protected void doStart() {
        executor = executorFactory.createSingleThreadedSchedulingExecutor("MieleDishwasher");
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
        Closeable.closeSafelyIfNotNull(logger, tokenSubscription, executor);
    }

    @Override
    public Closeable subscribeToEvents(Consumer<? super MieleEvent> eventHandler) {
        return whenStartedAndNotLifecycling(() -> {
            // TODO if this is ever used, make it start the stream on 1st subscription end close on last
            var eventStream = new EventStream();
            //noinspection resource as whole stream is closed
            eventStream.listeners.addListener(eventHandler);
            return eventStream;
        });
    }

    @Override
    public CompletableFuture<List<MieleProgram>> getPrograms() {
        return invokeGet("programs", new TypeToken<>() {});
    }

    @Override
    public CompletableFuture<MieleActions> getActions() {
        return invokeGet("actions", new TypeToken<>() {});
    }

    private <T> CompletableFuture<T> invokeGet(String endpoint, TypeToken<T> responseType) {
        return whenStartedAndNotLifecycling(() -> {
            logger.debug("[{}] invoking GET {}", deviceId, endpoint);
            var request = new Request.Builder()
                    .url(BASE_URL + "/devices/" + deviceId + "/" + endpoint)
                    .header("Authorization", "Bearer " + accessToken)
                    .get()
                    .build();
            return call(commandClient.newCall(request), responseType).whenComplete(logResult());
        });
    }

    @Override
    public CompletableFuture<Void> startProgram(int programId) {
        return invokePut("programs", builder -> builder.setProgramId(programId));
    }

    @Override
    public CompletableFuture<Void> powerOn() {
        return getActions().thenCompose(actions -> actions.powerOnAvailable()
                ? invokePut("actions", builder -> builder.setPowerOn(true))
                : CompletableFuture.completedFuture(null));
    }

    @Override
    public CompletableFuture<Void> powerOff() {
        return getActions().thenCompose(actions -> actions.powerOffAvailable()
                ? invokePut("actions", builder -> builder.setPowerOff(true))
                : CompletableFuture.completedFuture(null));
    }

    private CompletableFuture<Void> invokePut(String endpoint, Consumer<MieleCommand.Builder> commandBuilder) {
        return whenStartedAndNotLifecycling(() -> {
            var builder = MieleCommand.builder();
            commandBuilder.accept(builder);
            var command = builder.build();
            logger.debug("[{}] invoking PUT {}: {}", deviceId, endpoint, command);
            var request = new Request.Builder()
                    .url(BASE_URL + "/devices/" + deviceId + "/" + endpoint)
                    .header("Authorization", "Bearer " + accessToken)
                    .put(RequestBody.create(Json.stringify(command),
                                            MediaType.get(ContentTypes.CONTENT_TYPE_JSON)))
                    .addHeader(CONTENT_TYPE, ContentTypes.CONTENT_TYPE_JSON)
                    .build();

            return retryableOperationExecutor.withBackOffAndRetry(command.toString(), () -> call(commandClient.newCall(request), Void.class))
                                             .whenComplete(logResult());
        });
    }

    private BiConsumer<Object, ? super Throwable> logResult() {
        return (response, throwable) -> logger.debug("[{}] Result: {}", deviceId, throwable == null ? response : humanReadableMessage(throwable));
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface DeviceId {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }


    private class EventStream extends BaseIdempotentCloseable {
        private static final AtomicInteger streamIdGenerator = new AtomicInteger();
        public final Listeners<MieleEvent> listeners = new Listeners<>();
        private final Request request;
        private final BackOff reconnectBackoff;

        @Nullable
        private Call call;
        private volatile int streamId;

        protected EventStream() {
            request = new Request.Builder()
                    .url(BASE_URL + "/devices/" + deviceId + "/events")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "text/event-stream")
                    .build();
            reconnectBackoff = new ExponentialBackOff.Builder()
                    .setInitialIntervalMillis(1)
                    .setMaxIntervalMillis(10_000)
                    .setMaxElapsedTimeMillis(TimeUnit.DAYS.toMillis(3))
                    .setNanoClock(dateTimeProvider)
                    .build();
            executor.submit(this::connect);
        }

        private void connect() {
            streamId = streamIdGenerator.incrementAndGet();
            logger.info("[{}][{}] connecting to SSE stream", deviceId, streamId);
            call = eventingClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    executor.submit(() -> {
                        if (call == EventStream.this.call) {
                            reconnect(humanReadableMessage(e));
                        } else {
                            logger.warn("[{}] got failure callback for call {} while active call is {} and active streamId is {}",
                                        deviceId, call, EventStream.this.call, streamId);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // ! HTTP thread !
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful()) {
                            logger.warn("[{}] failed to open SSE stream: response code {}{}",
                                        deviceId, response.code(), (responseBody == null ? "" : ", body: " + responseBody.string()));
                            return;
                        }

                        logger.info("[{}][{}] connected to SSE stream", deviceId, streamId);

                        if (responseBody == null) {
                            reconnect("response is empty");
                        } else {
                            readLoop(responseBody);
                        }
                    }
                }
            });
        }

        private void readLoop(ResponseBody responseBody) {
            // Read the SSE events line by line
            // The SSE stream remains open indefinitely, so read it continuously
            String line;
            try {
                @Nullable String eventType = null;
                while ((line = responseBody.source().readUtf8Line()) != null) {
                    logger.debug("[{}][{}] event: {}", deviceId, streamId, line);
                    var eventPrefix = "event: ";
                    var dataPrefix = "data: ";
                    if (line.startsWith(eventPrefix)) {
                        eventType = line.substring(eventPrefix.length());
                    } else if (line.startsWith(dataPrefix)) {
                        String eventData = line.substring(dataPrefix.length());
                        if (eventType == null) {
                            logger.info("[{}][{}] unexpected event data, not preceded with event type: {}", deviceId, streamId, eventData);
                        } else {
                            handleEvent(eventType, eventData);
                            eventType = null;
                        }
                    }
                }
            } catch (IOException e) {
                if (!isClosed()) {
                    executor.submit(() -> reconnect(humanReadableMessage(e)));
                }
            }
        }

        private void reconnect(String failureReason) {
            var nextBackOffMillis = reconnectBackoff.nextBackOffMillis();
            if (nextBackOffMillis == BackOff.STOP) {
                logger.error("[{}][{}] stream failure: {}, will not re-connect", deviceId, streamId, failureReason);
                closeStream();
            } else {
                logger.info("[{}][{}] stream failure: {}, will close and re-connect in {}ms", deviceId, streamId, failureReason, nextBackOffMillis);
                closeStream();
                executor.schedule(ofMillis(nextBackOffMillis), this::connect);
            }
        }

        private void handleEvent(String eventType, String data) {
            executor.submit(() -> {
                try {
                    switch (eventType) {
                        case "device" -> handle(Json.parse(data, MieleDevice.class));
                        case "actions" -> handle(Json.parse(data, new TypeToken<Map<String, MieleActions>>() {}).get(deviceId));
                        default -> logger.debug("[{}][{}] ignoring unknown event type {}", deviceId, streamId, eventType);
                    }
                } catch (RuntimeException e) {
                    logger.error("[{}][{}] event handler failed", deviceId, streamId, e);
                }
            });
        }

        @Override
        protected final void doClose() {
            executor.submit(this::closeStream);
        }

        private void closeStream() {
            if (call != null) {
                logger.info("[{}][{}] closing SSE stream", deviceId, streamId);
                call.cancel();
                logger.info("[{}][{}] SSE stream closed", deviceId, streamId);
            }
            call = null;
            streamId = -1;
        }

        private void handle(MieleEvent event) {
            listeners.notify(event);
        }
    }
}
