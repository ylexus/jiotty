package net.yudichev.jiotty.connector.google.maps;

import net.yudichev.jiotty.common.geo.LatLon;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GeocodingService {
    CompletableFuture<List<LatLon>> geocode(String address);
}
