package net.yudichev.jiotty.connector.icloud.calendar;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CalendarService {
    CompletableFuture<List<Calendar>> retrieveCalendars();
}
