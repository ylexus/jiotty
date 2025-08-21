package net.yudichev.jiotty.connector.icloud.calendar;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Calendar {
    String id();

    String name();

    CompletableFuture<List<CalendarEvent>> fetchEvents(Instant from, Instant to);
}
