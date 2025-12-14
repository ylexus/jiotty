package net.yudichev.jiotty.energy;

import net.yudichev.jiotty.common.lang.Closeable;

import java.util.Optional;
import java.util.function.Consumer;

public interface EnergyPriceService {
    Optional<Prices> getPrices();

    Closeable subscribeToPrices(Consumer<Prices> consumer);
}
