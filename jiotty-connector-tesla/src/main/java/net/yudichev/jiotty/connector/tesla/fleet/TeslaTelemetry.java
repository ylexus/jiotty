package net.yudichev.jiotty.connector.tesla.fleet;

import net.yudichev.jiotty.common.lang.Closeable;

import java.util.function.Consumer;

public interface TeslaTelemetry {
    Closeable subscribeToMetrics(Consumer<TelemetryField> listener);

    Closeable subscribeToConnectivity(Consumer<TelemetryConnectivityEvent> listener);
}
