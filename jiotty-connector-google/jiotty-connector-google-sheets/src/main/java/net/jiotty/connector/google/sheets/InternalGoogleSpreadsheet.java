package net.jiotty.connector.google.sheets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import net.jiotty.common.rest.RestClients;
import okhttp3.*;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static net.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.jiotty.connector.google.sheets.Bindings.Internal;

final class InternalGoogleSpreadsheet implements GoogleSpreadsheet {
    private final Credential credential;
    private final Sheets sheets;
    private final Spreadsheet spreadsheet;
    private final OkHttpClient httpClient;

    @Inject
    InternalGoogleSpreadsheet(Credential credential,
                              @Internal Sheets sheets,
                              @Assisted Spreadsheet spreadsheet) {
        this.credential = checkNotNull(credential);
        this.sheets = checkNotNull(sheets);
        this.spreadsheet = checkNotNull(spreadsheet);
        httpClient = RestClients.newClient();
    }

    @Override
    public CompletableFuture<Void> updateRange(String range, Object value) {
        return supplyAsync(() -> getAsUnchecked(() -> sheets.spreadsheets().values()
                .update(spreadsheet.getSpreadsheetId(), range, new ValueRange().setValues(ImmutableList.of(ImmutableList.of(value))))
                .setValueInputOption("USER_ENTERED")
                .execute()))
                .thenApply(response -> null);
    }

    @Override
    public CompletableFuture<List<List<Object>>> getRangeValues(String range) {
        return supplyAsync(() -> getAsUnchecked(() -> sheets.spreadsheets().values()
                .get(spreadsheet.getSpreadsheetId(), range)
                .execute()))
                .thenApply(ValueRange::getValues);
    }

    @Override
    public CompletableFuture<byte[]> export(String sheetName) {
        Integer sheetId = spreadsheet.getSheets().stream()
                .filter(sheet -> sheet.getProperties().getTitle().equals(sheetName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No sheet with title " + sheetName))
                .getProperties()
                .getSheetId();
        checkState(sheetId != null);
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        httpClient.newCall(new Request.Builder()
                .url(HttpUrl.get("https://docs.google.com/spreadsheets/d/" + spreadsheet.getSpreadsheetId() + "/export").newBuilder()
                        .addQueryParameter("format", "pdf")
                        .addQueryParameter("gid", sheetId.toString())
                        .addQueryParameter("size", "7")
                        .addQueryParameter("portrait", "true")
                        .build())
                .header("authorization", "Bearer " + credential.getAccessToken())
                .get()
                .build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        future.completeExceptionally(e);
                    }

                    @Override
                    public void onResponse(Call call, okhttp3.Response response) {
                        ResponseBody responseBody = checkNotNull(response.body());
                        try {
                            future.complete(responseBody.bytes());
                        } catch (RuntimeException | IOException e) {
                            future.completeExceptionally(new RuntimeException("failed to get response body", e));
                        }
                    }
                });
        return future;
    }
}
