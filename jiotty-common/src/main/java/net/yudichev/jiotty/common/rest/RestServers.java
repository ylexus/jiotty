package net.yudichev.jiotty.common.rest;

import com.google.common.base.Throwables;
import net.yudichev.jiotty.common.lang.Json;
import net.yudichev.jiotty.common.lang.MoreThrowables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public final class RestServers {
    private static final Logger logger = LoggerFactory.getLogger(RestServers.class);

    private RestServers() {
    }

    public static Object withErrorsHandledJson(String handlerName, Response response, CompletableFuture<?> handler) {
        response.header("Content-Type", "application/json");
        return withErrorsHandled(handlerName, handler,
                () -> Json.object()
                        .put("success", "true")
                        .toString(),
                message -> Json.object()
                        .put("success", "false")
                        .put("errorText", message)
                        .toString());
    }

    private static Object withErrorsHandled(String handlerName,
                                            CompletableFuture<?> handler,
                                            Supplier<Object> successSupplier,
                                            Function<String, Object> errorFactory) {
        try {
            MoreThrowables.asUnchecked(() -> handler.get(3, TimeUnit.MINUTES));
            return successSupplier.get();
        } catch (RuntimeException e) {
            logger.error("Failed to execute REST handler {}", handlerName, e);
            String message = Throwables.getRootCause(e).getMessage();
            return errorFactory.apply(message);
        }
    }
}
