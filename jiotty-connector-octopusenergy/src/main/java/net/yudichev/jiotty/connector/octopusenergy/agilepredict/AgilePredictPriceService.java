package net.yudichev.jiotty.connector.octopusenergy.agilepredict;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AgilePredictPriceService {
    CompletableFuture<List<AgilePredictPrice>> getPrices(String region, int dayCount);
}
