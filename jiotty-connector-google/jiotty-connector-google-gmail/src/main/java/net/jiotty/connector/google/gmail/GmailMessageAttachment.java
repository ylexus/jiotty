package net.jiotty.connector.google.gmail;

import java.util.concurrent.CompletableFuture;

public interface GmailMessageAttachment {
    String getFileName();

    String getMimeType();

    CompletableFuture<byte[]> getData();
}
