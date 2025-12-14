package net.yudichev.jiotty.energy;

import java.util.List;

/**
 * @param idxOfPredictedPriceStart if negative then all the prices are predicted; if >= {@code pricePerInterval.size()} then all prices are actual
 */
public record PriceProfile(int intervalLengthSec, int idxOfPredictedPriceStart, List<Double> pricePerInterval) {}
