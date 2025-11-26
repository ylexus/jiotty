package net.yudichev.jiotty.connector.google.gmail;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import net.yudichev.jiotty.connector.google.gmail.Bindings.GmailService;

import java.util.concurrent.CompletableFuture;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;

final class InternalGmailMessageAttachment implements GmailMessageAttachment {
    private final Gmail gmail;
    private final Message message;
    private final MessagePart messagePart;

    @Inject
    InternalGmailMessageAttachment(@GmailService Gmail gmail,
                                   @Assisted Message message,
                                   @Assisted MessagePart messagePart) {
        this.gmail = checkNotNull(gmail);
        this.message = checkNotNull(message);
        this.messagePart = checkNotNull(messagePart);
    }

    @Override
    public String getFileName() {
        return messagePart.getFilename();
    }

    @Override
    public String getMimeType() {
        return messagePart.getMimeType();
    }

    @Override
    public CompletableFuture<byte[]> getData() {
        MessagePartBody body = messagePart.getBody();
        return body.getAttachmentId() != null ?
                supplyAsync(() -> getAsUnchecked(() ->
                                                         gmail.users()
                                                              .messages()
                                                              .attachments()
                                                              .get(Constants.ME, message.getId(), body.getAttachmentId())
                                                              .execute()
                                                              .decodeData())) :
                completedFuture(body.decodeData());
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("fileName", messagePart.getFilename())
                .add("mimeType", messagePart.getMimeType())
                .toString();
    }
}
