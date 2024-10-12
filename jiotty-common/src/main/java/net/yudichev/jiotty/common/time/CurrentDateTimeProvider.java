package net.yudichev.jiotty.common.time;

import net.yudichev.jiotty.common.lang.backoff.NanoClock;

import java.time.Instant;
import java.time.LocalDateTime;

public interface CurrentDateTimeProvider extends NanoClock {
    LocalDateTime currentDateTime();

    Instant currentInstant();
}
