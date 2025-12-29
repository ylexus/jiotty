package net.yudichev.jiotty.connector.tesla.fleet;

import com.google.inject.assistedinject.Assisted;

public interface TeslaTelemetryFactory {
    TeslaTelemetry create(@Assisted("vin") String vin);
}
