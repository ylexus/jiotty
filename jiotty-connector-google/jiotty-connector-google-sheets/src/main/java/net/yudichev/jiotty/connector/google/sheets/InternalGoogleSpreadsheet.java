package net.yudichev.jiotty.connector.google.sheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import net.yudichev.jiotty.common.rest.RestClients;
import net.yudichev.jiotty.connector.google.common.GoogleAuthorization;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.yudichev.jiotty.connector.google.common.impl.Bindings.Authorization;
import static net.yudichev.jiotty.connector.google.sheets.Bindings.Internal;

final class InternalGoogleSpreadsheet implements GoogleSpreadsheet {
    private final Sheets sheets;
    private final Spreadsheet spreadsheet;
    private final Provider<GoogleAuthorization> googleAuthorizationProvider;
    private final OkHttpClient httpClient;

    @Inject
    InternalGoogleSpreadsheet(@Internal Sheets sheets,
                              @Assisted Spreadsheet spreadsheet,
                              @Authorization Provider<GoogleAuthorization> googleAuthorizationProvider) {
        this.sheets = checkNotNull(sheets);
        this.spreadsheet = checkNotNull(spreadsheet);
        this.googleAuthorizationProvider = checkNotNull(googleAuthorizationProvider);
        httpClient = RestClients.newClient();
    }

    @Override
    public CompletableFuture<Void> updateRange(String range, Object value) {
        return supplyAsync(() -> getAsUnchecked(() -> sheets.spreadsheets().values()
                                                            .update(spreadsheet.getSpreadsheetId(),
                                                                    range,
                                                                    new ValueRange().setValues(ImmutableList.of(ImmutableList.of(value))))
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
                                   .header("authorization", "Bearer " + googleAuthorizationProvider.get().getCredential().getAccessToken())
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
