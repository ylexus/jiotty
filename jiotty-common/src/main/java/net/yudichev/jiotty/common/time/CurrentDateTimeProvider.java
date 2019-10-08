package net.yudichev.jiotty.common.time;

import java.time.Instant;
import java.time.LocalDateTime;

public interface CurrentDateTimeProvider {
    LocalDateTime currentDateTime();

    Instant currentInstant();
}
