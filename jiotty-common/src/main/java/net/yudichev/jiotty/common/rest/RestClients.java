package net.yudichev.jiotty.common.rest;

import com.fasterxml.jackson.databind.JsonNode;
import net.yudichev.jiotty.common.lang.Json;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

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
        AtomicReference<OkHttpClient> clientRef = new AtomicReference<>();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(DEFAULT_HTTP_TIMEOUT)
                .callTimeout(DEFAULT_HTTP_TIMEOUT)
                .readTimeout(DEFAULT_HTTP_TIMEOUT)
                .writeTimeout(DEFAULT_HTTP_TIMEOUT)
                .addInterceptor(new RedirectSupportInterceptor(clientRef::get));
        customizer.accept(builder);
        OkHttpClient client = builder.build();
        clientRef.set(client);
        return client;
    }

    public static <T> CompletableFuture<T> call(Request request, Class<? extends T> responseType) {
        return call(newClient().newCall(request), responseType);
    }

    public static <T> CompletableFuture<T> call(Call theCall, Class<? extends T> responseType) {
        return call(theCall, responseType, DEFAULT_CALL_RETRY_COUNT);
    }

    public static <T> CompletableFuture<T> call(Call theCall, Class<? extends T> responseType, int retryCount) {
        CompletableFuture<T> future = new CompletableFuture<>();
        theCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                logger.debug("Call failed: {}, retries left: {}", call, retryCount, e);
                if (retryCount == 0) {
                    future.completeExceptionally(new RuntimeException("call failed: " + call, e));
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
                ResponseBody responseBody = requireNonNull(response.body());
                try {
                    String responseString = responseBody.string();
                    future.complete(Json.parse(responseString, responseType));
                } catch (RuntimeException | IOException e) {
                    future.completeExceptionally(new RuntimeException("failed to parse response body to json: " + responseBody, e));
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
}
