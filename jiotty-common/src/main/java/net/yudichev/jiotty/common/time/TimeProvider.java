package net.yudichev.jiotty.common.time;

import java.time.Instant;
import java.time.LocalDateTime;

public final class TimeProvider implements CurrentDateTimeProvider {
    @Override
    public LocalDateTime currentDateTime() {
        return LocalDateTime.now();
    }

    @Override
    public Instant currentInstant() {
        return Instant.now();
    }

    @Override
    public long nanoTime() {
        return System.nanoTime();
    }
}
