package net.yudichev.jiotty.connector.world.sun;

import net.yudichev.jiotty.common.geo.LatLon;

public interface SunriseSunsetServiceFactory {
    SunriseSunsetService create(LatLon coordinates);
}
