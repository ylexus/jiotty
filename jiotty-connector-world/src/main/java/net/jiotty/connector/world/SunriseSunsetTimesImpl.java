package net.jiotty.connector.world;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static net.jiotty.common.rest.RestClients.call;
import static net.jiotty.common.rest.RestClients.newClient;
import static okhttp3.HttpUrl.parse;

final class SunriseSunsetTimesImpl implements SunriseSunsetTimes {
    private static final String API_URL = "https://api.sunrise-sunset.org/json";
    private final OkHttpClient client;

    SunriseSunsetTimesImpl() {
        client = newClient(OkHttpClient.Builder::build);
    }

    @Override
    public CompletableFuture<SunriseSunsetData> getCurrentSunriseSunset(WorldCoordinates worldCoordinates) {
        return call(client.newCall(new Request.Builder()
                .url(checkNotNull(parse(API_URL)).newBuilder()
                        .addQueryParameter("lat", Double.toString(worldCoordinates.getLatitude()))
                        .addQueryParameter("lng", Double.toString(worldCoordinates.getLongitude()))
                        .addQueryParameter("formatted", "0")
                        .build())
                .get()
                .build()), SunriseSunsetResponse.class)
                .thenApply(sunriseSunsetResponse -> {
                    checkState("OK".equals(sunriseSunsetResponse.status()), "status not OK in response %s", sunriseSunsetResponse);
                    checkState(sunriseSunsetResponse.results().isPresent(), "no results in response %s", sunriseSunsetResponse);
                    return sunriseSunsetResponse.results().get();
                });
    }
}
