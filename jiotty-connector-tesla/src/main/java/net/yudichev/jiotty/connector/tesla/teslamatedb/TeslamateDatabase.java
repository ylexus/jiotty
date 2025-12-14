package net.yudichev.jiotty.connector.tesla.teslamatedb;

import net.yudichev.jiotty.common.geo.LatLon;
import net.yudichev.jiotty.common.lang.Closeable;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TeslamateDatabase {
    Connection connect();

    interface Connection extends Closeable {

        CompletableFuture<List<HistoricalDrive>> queryDrives(long computationId,
                                                             Instant startInstant,
                                                             Instant endInstant,
                                                             LatLon startLocation,
                                                             LatLon endLocation,
                                                             double withinMeters);
    }
}
