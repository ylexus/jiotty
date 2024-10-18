package net.yudichev.jiotty.connector.shelly;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ShellyPlug {
    CompletableFuture<Boolean> powerOn();

    CompletableFuture<Boolean> powerOff();

    CompletableFuture<SwitchStatus> getStatus();

    ConsumptionMeasurement startMeasuringConsumption(Consumer<String> errorHandler);

    interface ConsumptionMeasurement {
        Optional<ConsumptionCurve> stop();
    }

    record ConsumptionCurve(Instant firstMinuteTimestamp, List<Double> mWHoursPerMinute) {}
}
