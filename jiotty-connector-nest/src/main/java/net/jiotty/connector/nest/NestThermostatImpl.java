package net.jiotty.connector.nest;

import com.google.inject.BindingAnnotation;
import net.jiotty.common.inject.BaseLifecycleComponent;
import net.jiotty.common.lang.Json;
import net.jiotty.common.rest.ContentTypes;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.CompletableFuture;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.jiotty.common.rest.RestClients.call;
import static net.jiotty.common.rest.RestClients.newClient;

final class NestThermostatImpl extends BaseLifecycleComponent implements NestThermostat {
    private final String hvacModeUrl;
    private final OkHttpClient client;
    private final String authorization;

    @Inject
    NestThermostatImpl(String accessToken, String deviceId) {
        this.authorization = "Bearer " + accessToken;
        this.client = newClient(builder -> builder
                .authenticator((route, response) -> response.request().newBuilder()
                        .header(AUTHORIZATION, authorization)
                        .build())
                .build());
        hvacModeUrl = "https://developer-api.nest.com/devices/thermostats/" + deviceId + "/hvac_mode";
    }

    @Override
    public CompletableFuture<Mode> currentMode() {
        return call(client.newCall(new Request.Builder()
                .url(hvacModeUrl)
                .get()
                .addHeader(CONTENT_TYPE, ContentTypes.CONTENT_TYPE_JSON)
//                .addHeader(AUTHORIZATION, "Bearer " + accessToken)
                .build()), Mode.class);
    }

    @Override
    public CompletableFuture<Mode> setMode(Mode mode) {
        Request request = new Request.Builder()
                .url(hvacModeUrl)
                .put(RequestBody.create(MediaType.get(ContentTypes.CONTENT_TYPE_JSON), Json.stringify(mode.id())))
                .addHeader(CONTENT_TYPE, ContentTypes.CONTENT_TYPE_JSON)
//                .addHeader(AUTHORIZATION, accessToken)
                .build();

        return call(client.newCall(request), Mode.class);
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface AccessToken {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface DeviceId {
    }
}
