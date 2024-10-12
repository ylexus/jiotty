package net.yudichev.jiotty.connector.octopusenergy;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface OctopusEnergy {

    CompletableFuture<List<StandardUnitRate>> getAgilePrices(Instant periodFrom, Instant periodTo);
}
