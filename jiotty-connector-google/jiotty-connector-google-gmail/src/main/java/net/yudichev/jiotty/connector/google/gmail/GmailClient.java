package net.yudichev.jiotty.connector.google.gmail;

import jakarta.mail.internet.MimeMessage;
import net.yudichev.jiotty.common.lang.Closeable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface GmailClient {
    // TODO on all google services, allow passing the executor
    Closeable subscribe(String query, Consumer<GmailMessage> handler);

    CompletableFuture<Void> send(MessageComposer messageComposer);

    interface MessageComposer {
        void accept(MimeMessage message);
    }
}
