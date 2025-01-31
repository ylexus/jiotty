package net.yudichev.jiotty.common.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.MoreExecutors;
import net.yudichev.jiotty.common.lang.Json;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static net.yudichev.jiotty.common.lang.Closeable.closeSafelyIfNotNull;

public final class RestClients {
    private static final Logger logger = LoggerFactory.getLogger(RestClients.class);

    private static final int DEFAULT_CALL_RETRY_COUNT = 3;
    private static final Duration DEFAULT_HTTP_TIMEOUT = Duration.ofSeconds(60);

    private RestClients() {
    }

    public static OkHttpClient newClient() {
        return newClient(builder -> {});
    }

    public static OkHttpClient newClient(Consumer<? super OkHttpClient.Builder> customizer) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(DEFAULT_HTTP_TIMEOUT)
                .callTimeout(DEFAULT_HTTP_TIMEOUT)
                .readTimeout(DEFAULT_HTTP_TIMEOUT)
                .writeTimeout(DEFAULT_HTTP_TIMEOUT);
        customizer.accept(builder);
        return builder.build();
    }

    public static <T> CompletableFuture<T> call(Call theCall, Class<? extends T> responseType) {
        return call(theCall, responseType, DEFAULT_CALL_RETRY_COUNT);
    }

    public static <T> CompletableFuture<T> call(Call theCall, TypeToken<? extends T> responseType) {
        return call(theCall, responseType, DEFAULT_CALL_RETRY_COUNT);
    }

    public static <T> CompletableFuture<T> call(Call theCall, Class<? extends T> responseType, int retryCount) {
        return call(theCall, TypeToken.of(responseType), retryCount);
    }

    public static <T> CompletableFuture<T> call(Call theCall, TypeToken<? extends T> responseType, int retryCount) {
        CompletableFuture<T> future = new CompletableFuture<>();
        theCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                logger.debug("Call failed: {}, retries left: {}", call, retryCount, e);
                if (retryCount == 0) {
                    future.completeExceptionally(new RuntimeException("call failed: " + call.request(), e));
                } else {
                    call(call.clone(), responseType, retryCount - 1)
                            .whenComplete((result, exception) -> {
                                if (exception == null) {
                                    future.complete(result);
                                } else {
                                    future.completeExceptionally(exception);
                                }
                            });
                }
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) {
                try (ResponseBody responseBody = response.body()) {
                    try {
                        if (response.isSuccessful()) {
                            if (response.code() == 204) { // no body
                                if (responseType.getType() == Void.class) {
                                    future.complete(null);
                                } else {
                                    future.completeExceptionally(new RuntimeException(
                                            "Response is successful but empty, however expected response type is " + responseType));
                                }
                            } else {
                                String responseString = requireNonNull(responseBody).string();
                                T responseData;
                                try {
                                    responseData = Json.parse(responseString, responseType);
                                    future.complete(responseData);
                                } catch (RuntimeException e) {
                                    future.completeExceptionally(new RuntimeException("Failed parsing response " + responseString, e));
                                }
                            }
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

    public static JsonNode getRequiredNode(JsonNode parentNode, String nodeName) {
        JsonNode childNode = parentNode.get(nodeName);
        checkState(childNode != null,
                   "no '%s' node in response: %s", nodeName, parentNode);
        return childNode;
    }

    public static String getRequiredNodeString(JsonNode parentNode, String nodeName) {
        JsonNode childNode = getRequiredNode(parentNode, nodeName);
        checkState(childNode.isTextual(), "node '%s' is not textual in %s", nodeName, parentNode);
        return childNode.asText();
    }

    public static int getRequiredNodeInt(JsonNode parentNode, String nodeName) {
        JsonNode childNode = getRequiredNode(parentNode, nodeName);
        checkState(childNode.isInt(), "node '%s' is not an integer in %s", nodeName, parentNode);
        return childNode.asInt();
    }

    public static long getRequiredNodeLong(JsonNode parentNode, String nodeName) {
        JsonNode childNode = getRequiredNode(parentNode, nodeName);
        checkState(childNode.isLong(), "node '%s' is not a long in %s", nodeName, parentNode);
        return childNode.asLong();
    }

    public static void shutdown(OkHttpClient client) {
        shutdown(client, Duration.ofSeconds(10));
    }

    public static void shutdown(OkHttpClient client, Duration timeout) {
        try {
            logger.debug("Shutting down {}", client);
            MoreExecutors.shutdownAndAwaitTermination(client.dispatcher().executorService(), timeout);
            client.connectionPool().evictAll();
            closeSafelyIfNotNull(logger, client.cache());
        } catch (RuntimeException e) {
            logger.warn("Failed to gracefully shut down client {} in {}", client, timeout, e);
        }
    }
}
