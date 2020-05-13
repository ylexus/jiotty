package net.yudichev.jiotty.connector.slide;

import com.google.common.reflect.TypeToken;
import net.yudichev.jiotty.common.async.ExecutorFactory;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.CompletableFutures;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.lang.Json.object;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.yudichev.jiotty.common.rest.ContentTypes.CONTENT_TYPE_JSON;
import static net.yudichev.jiotty.common.rest.RestClients.call;
import static net.yudichev.jiotty.common.rest.RestClients.newClient;
import static net.yudichev.jiotty.connector.slide.Bindings.Email;
import static net.yudichev.jiotty.connector.slide.Bindings.Password;

final class SlideServiceImpl extends BaseLifecycleComponent implements SlideService {
    private static final Logger logger = LoggerFactory.getLogger(SlideServiceImpl.class);

    private final OkHttpClient client = newClient();
    private final ExecutorFactory executorFactory;
    private final String email;
    private final String password;
    private String accessToken;
    private SchedulingExecutor executor;

    @Inject
    SlideServiceImpl(ExecutorFactory executorFactory,
                     @Email String email,
                     @Password String password) {
        this.executorFactory = checkNotNull(executorFactory);
        this.email = checkNotNull(email);
        this.password = checkNotNull(password);
    }

    @Override
    public CompletableFuture<SlideInfo> getSlideInfo(long slideId) {
        logger.debug("Getting slide info ({})", slideId);
        return call(client.newCall(new Request.Builder()
                        .url("https://api.goslide.io/api/slide/" + slideId + "/info")
                        .get()
                        .header("authorization", "Bearer " + accessToken)
                        .build()),
                new TypeToken<SlideResponse<SlideInfo>>() {})
                .thenApply(response -> response.dataOrThrow("Failed to obtain slide info for id " + slideId));
    }

    @Override
    public CompletableFuture<Void> setSlidePosition(long slideId, double position) {
        logger.debug("Set slide position for {} to {}", slideId, position);
        return call(client.newCall(new Request.Builder()
                        .url("https://api.goslide.io/api/slide/" + slideId + "/position")
                        .post(RequestBody.create(object()
                                        .put("pos", position)
                                        .toString(),
                                MediaType.get(CONTENT_TYPE_JSON)))
                        .header("authorization", "Bearer " + accessToken)
                        .build()),
                new TypeToken<SlideResponse<Object>>() {})
                .thenAccept(response -> response.dataOrThrow("Failed to set slide position for id " + slideId));
    }

    @Override
    protected void doStart() {
        executor = executorFactory.createSingleThreadedSchedulingExecutor("slide-service");
        AuthenticationResponse authenticationResponse = getAsUnchecked(() -> issueRefreshAccessTokenRequest().get(10, TimeUnit.SECONDS));
        accessToken = authenticationResponse.accessToken();
        Duration accessTokenRefreshPeriod = Duration.ofSeconds(authenticationResponse.expiresInSeconds()).minus(Duration.ofDays(1));
        logger.debug("Access token refresh period {}", accessTokenRefreshPeriod);
        executor.scheduleAtFixedRate(accessTokenRefreshPeriod, this::refreshAccessToken);
    }

    @Override
    protected void doStop() {
        executor.close();
    }

    private CompletableFuture<AuthenticationResponse> issueRefreshAccessTokenRequest() {
        logger.info("Refreshing access token");
        return call(client.newCall(new Request.Builder()
                        .url("https://api.goslide.io/api/auth/login")
                        .post(RequestBody.create(object()
                                        .put("email", email)
                                        .put("password", password)
                                        .toString(),
                                MediaType.get(CONTENT_TYPE_JSON)))
                        .build()),
                AuthenticationResponse.class);
    }

    private void refreshAccessToken() {
        issueRefreshAccessTokenRequest()
                .thenAccept(authenticationResponse -> whenStartedAndNotLifecycling(() -> {accessToken = authenticationResponse.accessToken();}))
                .whenComplete(CompletableFutures.logErrorOnFailure(logger, "failed to refresh slide access token"));
    }
}
