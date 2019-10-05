package net.jiotty.connector.google.sheets;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.google.common.collect.Iterables.getOnlyElement;

public interface GoogleSpreadsheet {
    CompletableFuture<Void> updateRange(String range, Object value);

    CompletableFuture<List<List<Object>>> getRangeValues(String range);

    default CompletableFuture<Object> getSingleCellValue(String range) {
        return getRangeValues(range).thenApply(GoogleSpreadsheet::singleCell);
    }

    CompletableFuture<byte[]> export(String sheetName);

    static Object singleCell(List<List<Object>> lists) {
        return getOnlyElement(getOnlyElement(lists));
    }
}
