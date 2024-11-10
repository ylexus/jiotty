package net.yudichev.jiotty.connector.miele;

import com.google.common.reflect.TypeToken;
import com.google.inject.BindingAnnotation;
import com.sun.net.httpserver.HttpServer;
import net.yudichev.jiotty.common.async.ExecutorFactory;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.Closeable;
import net.yudichev.jiotty.common.lang.Listeners;
import net.yudichev.jiotty.common.time.CurrentDateTimeProvider;
import net.yudichev.jiotty.common.varstore.VarStore;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static net.yudichev.jiotty.common.lang.CompletableFutures.logErrorOnFailure;
import static net.yudichev.jiotty.common.lang.MoreThrowables.asUnchecked;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.yudichev.jiotty.common.rest.RestClients.call;
import static net.yudichev.jiotty.common.rest.RestClients.newClient;

public class OAuth2TokenManagerImpl extends BaseLifecycleComponent implements OAuth2TokenManager {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2TokenManagerImpl.class);

    private static final String AUTH_BASE_URL = "https://api.mcs3.miele.com/thirdparty";
    private static final String TOKEN_URL = AUTH_BASE_URL + "/token";
    private static final Duration REFRESH_ADVANCE_PERIOD = Duration.ofDays(3);

    private final OkHttpClient httpClient = newClient();
    private final ExecutorFactory executorFactory;
    private final VarStore varStore;
    private final String clientId;
    private final String clientSecret;
    private final CurrentDateTimeProvider currentDateTimeProvider;
    private final Listeners<String> listeners = new Listeners<>();
    private final String varStoreKey;

    private OauthAccessToken currentToken;
    private SchedulingExecutor executor;
    /**
     * Non-nullness of this field means we are in the process of obtaining the initial token.
     */
    @Nullable
    private HttpServer httpServer;

    @Inject
    public OAuth2TokenManagerImpl(ExecutorFactory executorFactory,
                                  CurrentDateTimeProvider currentDateTimeProvider,
                                  VarStore varStore,
                                  @ClientID String clientId,
                                  @ClientSecret String clientSecret) {
        this.clientId = checkNotNull(clientId);
        this.clientSecret = checkNotNull(clientSecret);
        this.executorFactory = checkNotNull(executorFactory);
        this.currentDateTimeProvider = checkNotNull(currentDateTimeProvider);
        this.varStore = checkNotNull(varStore);
        varStoreKey = "MieleOauth2Token_" + clientId;
    }

    @Override
    protected void doStart() {
        executor = executorFactory.createSingleThreadedSchedulingExecutor("MieleOauth2");
        varStore.readValue(OauthAccessToken.class, varStoreKey)
                .ifPresentOrElse(accessToken -> {
                                     if (isExpired(accessToken)) {
                                         refreshAccessToken(accessToken.refreshToken());
                                     } else {
                                         setCurrentToken(accessToken);
                                     }
                                 },
                                 this::obtainAccessToken);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private boolean isExpired(OauthAccessToken accessToken) {
        return currentDateTimeProvider.currentInstant().isAfter(accessToken.expiryTime());
    }

    @Override
    protected void doStop() {
        Closeable.closeSafelyIfNotNull(logger, executor);
    }

    @Override
    public Closeable subscribeToAccessToken(Consumer<? super String> accessTokenHandler) {
        return whenStartedAndNotLifecycling(() -> listeners.addListener(executor,
                                                                        () -> Optional.ofNullable(currentToken).map(OauthAccessToken::accessToken),
                                                                        accessTokenHandler));
    }

    private void obtainAccessToken() {
        if (httpServer != null) {
            logger.info("[{}] already obtaining the access token", clientId);
            return;
        }

        // authorisation code based process, need to communicate with the user
        startRedirectHttpServer();

        logger.warn("Miele login required: {}/login?response_type=code&client_id={}&redirect_uri={}&scope=IDENTIFY_APPLIANCES",
                    AUTH_BASE_URL, clientId,
                    URLEncoder.encode("http://" + httpServer.getAddress().getHostName() + ':' + httpServer.getAddress().getPort() + "/logincallback"
                            , US_ASCII));
    }

    private void startRedirectHttpServer() {
        asUnchecked(() -> {
            httpServer = HttpServer.create(new InetSocketAddress(getOutboundAddress(), findFreeTcpPort()), 0);
            httpServer.createContext("/logincallback", exchange -> {
                logger.info("[{}] login callback received", clientId);
                var query = exchange.getRequestURI().getQuery();
                String authCode = splitQuery(query).get("code");
                String response;
                if (authCode != null) {
                    onAuthCodeReceived(authCode);
                    response = "Success";
                    exchange.sendResponseHeaders(200, response.length());
                } else {
                    response = "No 'code' in query " + query;
                    exchange.sendResponseHeaders(400, response.length());
                }
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            httpServer.createContext("/tokencallback", exchange -> {
                logger.info("[{}] token callback received", clientId);
                String response = "Success";
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            httpServer.setExecutor(executor); // creates a default executor
            httpServer.start();
        });
    }

    public static Map<String, String> splitQuery(String query) {
        var queryValuesByKey = new LinkedHashMap<String, String>();
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            queryValuesByKey.put(URLDecoder.decode(pair.substring(0, idx), US_ASCII),
                                 URLDecoder.decode(pair.substring(idx + 1), US_ASCII));
        }
        return queryValuesByKey;
    }

    private static InetAddress getOutboundAddress() {
        return getAsUnchecked(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                return socket.getLocalAddress();
            }
        });
    }

    private void onAuthCodeReceived(String authCode) {
        logger.info("[{}] auth code received", clientId);
        assert httpServer != null;
        requestToken(new FormBody.Builder()
                             .add("grant_type", "authorization_code")
                             .add("code", authCode)
                             .add("redirect_uri", "http://" + httpServer.getAddress() + "/tokencallback")
                             .add("client_id", clientId)
                             .add("client_secret", clientSecret)
                             .build());
    }

    public static int findFreeTcpPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }


    // Function to refresh the access token

    private void refreshAccessToken(String refreshToken) {
        requestToken(new FormBody.Builder()
                             .add("grant_type", "refresh_token")
                             .add("refresh_token", refreshToken)
                             .add("client_id", clientId)
                             .add("client_secret", clientSecret)
                             .build());
    }

    private void requestToken(RequestBody formBody) {
        var request = new Request.Builder().url(TOKEN_URL)
                                           .post(formBody)
                                           .build();

        Instant requestTime = currentDateTimeProvider.currentInstant();
        call(httpClient.newCall(request), new TypeToken<OauthAccessTokenResponse>() {})
                .thenAcceptAsync(response -> {
                    logger.info("[{}] token received", clientId);
                    // async is better not to delay processing; the stopping of the server is not important
                    executor.execute(() -> {
                        if (httpServer != null) {
                            httpServer.stop(10);
                            httpServer = null;
                        }
                    });
                    setCurrentToken(responseToToken(requestTime, response));
                    varStore.saveValue(varStoreKey, currentToken);
                    scheduleTokenRefresh();
                }, executor)
                .whenComplete(logErrorOnFailure(logger, "Failed to obtain token for client %s", clientId));
    }

    private OauthAccessToken responseToToken(Instant currentTime, OauthAccessTokenResponse response) {
        var refreshTime = currentTime.plusSeconds(response.expiresInSec())
                                     .minus(REFRESH_ADVANCE_PERIOD);
        checkArgument(refreshTime.isAfter(currentDateTimeProvider.currentInstant()),
                      "Client ID {}: OAuth2 response token's 'expires in, seconds' value {} is too small: the resulting token refresh time {} is in the past",
                      clientId, response.expiresInSec(), refreshTime);
        return OauthAccessToken.of(response.accessToken(), response.refreshToken(), refreshTime);
    }

    private void setCurrentToken(OauthAccessToken accessToken) {
        currentToken = accessToken;
        listeners.notify(currentToken.accessToken());
    }

    private void scheduleTokenRefresh() {
        var expiryDelay = Duration.between(currentDateTimeProvider.currentInstant(), currentToken.expiryTime());
        logger.info("[{}] will refresh token in {} ({})", clientId, expiryDelay, currentToken.expiryTime());
        executor.schedule(expiryDelay, () -> refreshAccessToken(currentToken.refreshToken()));
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface ClientID {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface ClientSecret {
    }
}
