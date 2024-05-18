package net.yudichev.jiotty.connector.slide;

import com.google.common.reflect.TypeToken;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static net.yudichev.jiotty.common.lang.Json.object;
import static net.yudichev.jiotty.common.rest.ContentTypes.CONTENT_TYPE_JSON;
import static net.yudichev.jiotty.common.rest.RestClients.call;
import static net.yudichev.jiotty.common.rest.RestClients.newClient;
import static net.yudichev.jiotty.connector.slide.Bindings.DeviceHost;

final class LocalSlideService implements SlideService {
    private static final Logger logger = LoggerFactory.getLogger(LocalSlideService.class);

    private final OkHttpClient client = newClient();
    private final String getInfoUrl;
    private final String setPostUrl;

    @Inject
    LocalSlideService(@DeviceHost String deviceHost) {
        getInfoUrl = "http://" + deviceHost + "/rpc/Slide.GetInfo";
        setPostUrl = "http://" + deviceHost + "/rpc/Slide.SetPos";
    }

    @Override
    public CompletableFuture<SlideInfo> getSlideInfo(long slideId, Executor executor) {
        logger.debug("Getting slide info ({})", slideId);
        return call(client.newCall(new Request.Builder().url(getInfoUrl).get().build()), new TypeToken<SlideInfo>() {})
                .thenApplyAsync(Function.identity(), executor);
    }

    @Override
    public CompletableFuture<Void> setSlidePosition(long slideId, double position, Executor executor) {
        logger.debug("Set slide {} position to {}", slideId, position);
        return call(client.newCall(new Request.Builder()
                        .url(setPostUrl)
                        .post(RequestBody.create(object()
                                        .put("pos", position)
                                        .toString(),
                                MediaType.get(CONTENT_TYPE_JSON)))
                        .build()),
                new TypeToken<LocalSlideResponse>() {})
                .thenAcceptAsync(response -> response.response()
                        .map(responseMsg -> "success".equals(responseMsg) ? null : responseMsg)
                        .ifPresent(error -> {throw new RuntimeException("Failed to set slide position: " + error);}));
    }
}
