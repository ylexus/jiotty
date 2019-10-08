package net.yudichev.jiotty.connector.google.sheets;

import java.util.concurrent.CompletableFuture;

public interface GoogleSheetsClient {
    CompletableFuture<GoogleSpreadsheet> getSpreadsheet(String spreadsheetId);
}
