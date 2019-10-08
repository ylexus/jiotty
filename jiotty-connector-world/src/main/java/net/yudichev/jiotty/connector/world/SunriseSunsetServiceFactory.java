package net.yudichev.jiotty.connector.world;

public interface SunriseSunsetServiceFactory {
    SunriseSunsetService create(WorldCoordinates worldCoordinates);
}
