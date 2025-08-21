package net.yudichev.jiotty.common.geo;

public record LatLon(double lat, double lon) {
    @Override
    public String toString() {
        return new StringBuilder(32).append('{').append(lat).append(',').append(lon).append('}').toString();
    }
}
