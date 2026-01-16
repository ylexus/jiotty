package net.yudichev.jiotty.connector.google.maps;

import net.yudichev.jiotty.common.geo.LatLon;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GeocodingService {
    /// @return list of latitude and longitude coordinates for the given address; multiple entries in the list mean more than one match; no entries mean no
    /// match
    CompletableFuture<List<LatLon>> geocode(String address);
}
