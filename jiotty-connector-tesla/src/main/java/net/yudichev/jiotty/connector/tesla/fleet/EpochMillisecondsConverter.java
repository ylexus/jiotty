package net.yudichev.jiotty.connector.tesla.fleet;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.time.Instant;

import static net.yudichev.jiotty.common.lang.EvenMoreObjects.mapIfNotNull;

class EpochMillisecondsConverter extends StdConverter<Long, Instant> {
    @Override
    public Instant convert(Long value) {
        return mapIfNotNull(value, Instant::ofEpochMilli);
    }
}
