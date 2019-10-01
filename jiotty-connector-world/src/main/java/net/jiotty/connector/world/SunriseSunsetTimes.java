package net.jiotty.connector.world;

import java.util.concurrent.CompletableFuture;

/**
 * Courtesy of sunrise-sunset.org.
 */
public interface SunriseSunsetTimes {
    CompletableFuture<SunriseSunsetData> getCurrentSunriseSunset(WorldCoordinates worldCoordinates);
}
