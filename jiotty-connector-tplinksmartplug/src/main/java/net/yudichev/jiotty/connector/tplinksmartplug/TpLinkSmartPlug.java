package net.yudichev.jiotty.connector.tplinksmartplug;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.appliance.Appliance;
import net.yudichev.jiotty.appliance.Command;
import net.yudichev.jiotty.appliance.PowerCommand;
import net.yudichev.jiotty.common.async.ExecutorFactory;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.Closeable;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.time.Duration.ZERO;
import static java.time.Duration.ofDays;
import static net.yudichev.jiotty.appliance.PowerCommand.OFF;
import static net.yudichev.jiotty.appliance.PowerCommand.ON;
import static net.yudichev.jiotty.common.lang.Json.object;
import static net.yudichev.jiotty.common.rest.ContentTypes.CONTENT_TYPE_JSON;
import static net.yudichev.jiotty.common.rest.RestClients.*;

final class TpLinkSmartPlug extends BaseLifecycleComponent implements Appliance {
    private static final Logger logger = LoggerFactory.getLogger(TpLinkSmartPlug.class);
    private static final Duration TOKEN_REFRESH_PERIOD = ofDays(14);
    private static final Map<Command, Integer> COMMAND_TO_STATE = ImmutableMap.of(
            ON, 1,
            OFF, 0
    );

    private final String username;
    private final String password;
    private final String termId;
    private final String deviceId;
    private final String name;
    private final ExecutorFactory executorFactory;
    private SchedulingExecutor executor;

    private CompletableFuture<String> tokenFuture;
    private Closeable tokenRefreshSchedule = Closeable.noop();

    @Inject
    TpLinkSmartPlug(@Username String username,
                    @Password String password,
                    @TermId String termId,
                    @DeviceId String deviceId,
                    @Name String name,
                    ExecutorFactory executorFactory) {
        this.username = checkNotNull(username);
        this.password = checkNotNull(password);
        this.termId = checkNotNull(termId);
        this.deviceId = checkNotNull(deviceId);
        this.name = checkNotNull(name);
        this.executorFactory = checkNotNull(executorFactory);
    }

    @Override
    public Set<? extends Command> getAllSupportedCommands() {
        return ImmutableSet.copyOf(PowerCommand.values());
    }

    @Override
    public CompletableFuture<?> execute(Command command) {
        return whenStartedAndNotLifecycling(() -> {
            //noinspection RedundantTypeArguments compiler is not coping
            return tokenFuture
                    .thenComposeAsync(token ->
                                    command.<CompletableFuture<?>>acceptOrFail((PowerCommand.Visitor<CompletableFuture<?>>) powerCommand ->
                                            post(token, COMMAND_TO_STATE.get(powerCommand))),
                            executor)
                    .thenRun(() -> logger.info("Plug {}: executed {}", name, command));
        });
    }

    @Override
    protected void doStart() {
        executor = executorFactory.createSingleThreadedSchedulingExecutor("tp-link-plug");
        tokenRefreshSchedule = executor.scheduleAtFixedRate(ZERO, TOKEN_REFRESH_PERIOD, this::refreshToken);
    }

    @Override
    protected void doStop() {
        tokenRefreshSchedule.close();
        executor.close();
    }

    private void refreshToken() {
        whenStartedAndNotLifecycling(() -> {
            logger.info("Plug {}: requesting token", name);
            tokenFuture = call(new Request.Builder()
                            .url(new HttpUrl.Builder()
                                    .scheme("https")
                                    .host("eu-wap.tplinkcloud.com")
                                    .build())
                            .post(RequestBody.create(object()
                                            .put("method", "login")
                                            .set("params", object()
                                                    .put("appType", "Kasa_Android")
                                                    .put("cloudUserName", username)
                                                    .put("cloudPassword", password)
                                                    .put("terminalUUID", UUID.randomUUID().toString()))
                                            .toString(),
                                    MediaType.get(CONTENT_TYPE_JSON)))
                            .build(),
                    JsonNode.class)
                    .thenApply(TpLinkSmartPlug::verifyResponse)
                    .thenApply(resultJsonNode -> {
                        logger.info("Plug {}: obtained token", name);
                        return getRequiredNodeString(resultJsonNode, "token");
                    });
        });
    }

    private CompletableFuture<?> post(String token, int state) {
        logger.debug("Setting plug {} state to {}", name, state);
        return call(new Request.Builder()
                        .url(new HttpUrl.Builder()
                                .scheme("https")
                                .host("eu-wap.tplinkcloud.com")
                                .addQueryParameter("token", token)
                                .addQueryParameter("appName", "Kasa_Android")
                                .addQueryParameter("termID", termId)
                                .addQueryParameter("appVer", "1.4.4.607")
                                .addQueryParameter("ospf", "Android 6.0.1")
                                .addQueryParameter("netType", "wifi")
                                .addQueryParameter("locale", "en_US")
                                .build())
                        .post(RequestBody.create(object()
                                        .put("method", "passthrough")
                                        .set("params", object()
                                                .put("deviceId", deviceId)
                                                .put("requestData", object()
                                                        .set("system", object()
                                                                .set("set_relay_state", object()
                                                                        .put("state", state)))
                                                        .toString()))
                                        .toString(),
                                MediaType.get(CONTENT_TYPE_JSON)))
                        .build(),
                JsonNode.class)
                .thenAccept(TpLinkSmartPlug::verifyResponse);
    }

    private static JsonNode verifyResponse(JsonNode response) {
        /*
        {
            "error_code": 0,
             "result": {
                "responseData": "{\"system\":{\"set_relay_state\":{\"err_code\":0}}}"
            }
         }
         */
        checkState(getRequiredNodeInt(response, "error_code") == 0, "response error code is not 0: %s", response);
        return getRequiredNode(response, "result");
    }

    @Retention(RUNTIME)
    @Target({FIELD, PARAMETER, METHOD})
    @BindingAnnotation
    @interface Username {
    }

    @Retention(RUNTIME)
    @Target({FIELD, PARAMETER, METHOD})
    @BindingAnnotation
    @interface Password {
    }

    @Retention(RUNTIME)
    @Target({FIELD, PARAMETER, METHOD})
    @BindingAnnotation
    @interface TermId {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface DeviceId {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Name {
    }
}
