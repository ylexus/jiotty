package net.yudichev.jiotty.connector.world.sun;

import net.yudichev.jiotty.common.geo.LatLon;

import java.util.concurrent.CompletableFuture;

/**
 * Courtesy of sunrise-sunset.org.
 */
public interface SunriseSunsetTimes {
    CompletableFuture<SunriseSunsetData> getCurrentSunriseSunset(LatLon worldCoordinates);
}
