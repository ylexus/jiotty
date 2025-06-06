package net.yudichev.jiotty.common.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import jakarta.servlet.http.HttpServletResponse;
import net.yudichev.jiotty.common.lang.Json;
import net.yudichev.jiotty.common.lang.MoreThrowables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public final class RestServers {
    private static final Logger logger = LoggerFactory.getLogger(RestServers.class);

    private RestServers() {
    }

    public static String withErrorsHandledJson(String handlerName, HttpServletResponse response, CompletableFuture<?> handler) {
        response.addHeader("Content-Type", "application/json");
        return withErrorsHandled(handlerName,
                                 handler,
                                 responseObj -> {
                                     ObjectNode factory = Json.object().put("success", "true");
                                     responseObj.ifPresent(theResponse -> factory.put("response", theResponse.toString()));
                                     return factory.toString();
                                 },
                                 message -> Json.object()
                                                .put("success", "false")
                                                .put("errorText", message)
                                                .toString());
    }

    private static String withErrorsHandled(String handlerName,
                                            CompletableFuture<?> handler,
                                            Function<Optional<Object>, String> successFactory,
                                            Function<String, String> errorFactory) {
        try {
            @Nullable Object response = MoreThrowables.getAsUnchecked(() -> handler.get(3, TimeUnit.MINUTES));
            return successFactory.apply(Optional.ofNullable(response));
        } catch (RuntimeException e) {
            logger.error("Failed to execute REST handler {}", handlerName, e);
            String message = Throwables.getRootCause(e).getMessage();
            return errorFactory.apply(message);
        }
    }
}
