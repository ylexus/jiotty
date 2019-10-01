package net.jiotty.common.time;

import java.time.Instant;
import java.time.LocalDateTime;

final class TimeProvider implements CurrentDateTimeProvider {
    @Override
    public LocalDateTime currentDateTime() {
        return LocalDateTime.now();
    }

    @Override
    public Instant currentInstant() {
        return Instant.now();
    }
}
