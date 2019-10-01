package net.jiotty.connector.fieldglass;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FieldglassTimeSheetsClient {
    CompletableFuture<List<TimeSheet>> getTimeSheetTable(String username, String password);
}
