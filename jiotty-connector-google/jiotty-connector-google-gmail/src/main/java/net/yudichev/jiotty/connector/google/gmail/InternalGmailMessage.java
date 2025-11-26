package net.yudichev.jiotty.connector.google.gmail;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import net.yudichev.jiotty.connector.google.gmail.Bindings.GmailService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;
import static net.yudichev.jiotty.common.lang.CompletableFutures.toFutureOfList;
import static net.yudichev.jiotty.common.lang.MoreThrowables.asUnchecked;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.yudichev.jiotty.connector.google.gmail.Constants.ME;

final class InternalGmailMessage implements GmailMessage {
    private static final Set<String> TO_STRING_HEADERS = ImmutableSet.of("From", "To", "Subject", "Date");
    private final Gmail gmail;
    private final InternalGmailObjectFactory internalGmailObjectFactory;
    private final Message message;
    private String asString;

    @Inject
    InternalGmailMessage(@GmailService Gmail gmail,
                         InternalGmailObjectFactory internalGmailObjectFactory,
                         @Assisted Message message) {
        this.gmail = checkNotNull(gmail);
        this.internalGmailObjectFactory = checkNotNull(internalGmailObjectFactory);
        this.message = checkNotNull(message);
    }

    @Override
    public Optional<String> getHeader(String name) {
        return message.getPayload().getHeaders().stream()
                      .filter(messagePartHeader -> messagePartHeader.getName().equals(name))
                      .map(MessagePartHeader::getValue)
                      .findFirst();

    }

    @Override
    public Collection<GmailMessageAttachment> getAttachments(Predicate<? super String> mimeTypePredicate) {
        return message.getPayload().getParts().stream()
                      .filter(messagePart -> mimeTypePredicate.test(messagePart.getMimeType()))
                      .map(messagePart -> internalGmailObjectFactory.createAttachment(message, messagePart))
                      .collect(toImmutableList());
    }

    @Override
    public CompletableFuture<Void> applyLabels(LabelsChange labelsChange) {
        return supplyAsync(() -> {
            asUnchecked(() -> gmail.users().messages().modify(ME, message.getId(),
                                                              new ModifyMessageRequest()
                                                                      .setAddLabelIds(toListOfIds(labelsChange.getLabelsToAdd()))
                                                                      .setRemoveLabelIds(toListOfIds(labelsChange.getLabelsToRemove())))
                                   .execute()
            );
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> applyLabels(LabelsChangeNames labelsChange) {
        return listLabels().thenCompose(gmailLabels -> {
            List<GmailLabel> labelsToRemove = labelsChange.getLabelsToRemove().stream()
                                                          .map(labelName -> gmailLabels.stream()
                                                                                       .filter(gmailLabel -> gmailLabel.getName().equals(labelName))
                                                                                       .findFirst())
                                                          .filter(Optional::isPresent)
                                                          .map(Optional::get)
                                                          .collect(toImmutableList());
            return labelsChange.getLabelsToAdd().stream()
                               .map(labelName -> gmailLabels.stream()
                                                            .filter(gmailLabel -> gmailLabel.getName().equals(labelName))
                                                            .findFirst()
                                                            .map(CompletableFuture::completedFuture)
                                                            .orElseGet(() -> createLabel(labelName)))
                               .collect(toFutureOfList())
                               .thenCompose(labelsToAdd ->
                                                    applyLabels(LabelsChange.builder()
                                                                            .setLabelsToAdd(labelsToAdd)
                                                                            .setLabelsToRemove(labelsToRemove)
                                                                            .build()));
        });
    }

    @Override
    public String toString() {
        if (asString == null) {
            asString = message.getPayload().getHeaders().stream()
                              .filter(messagePartHeader -> TO_STRING_HEADERS.contains(messagePartHeader.getName()))
                              .map(messagePartHeader -> messagePartHeader.getName() + "=" + messagePartHeader.getValue())
                              .collect(Collectors.joining(", "));
        }
        return asString;
    }

    private static List<String> toListOfIds(Collection<GmailLabel> labelsToAdd) {
        return labelsToAdd.stream().map(gmailLabel -> ((InternalGmailLabel) gmailLabel).getId()).collect(toList());
    }

    private CompletableFuture<Collection<GmailLabel>> listLabels() {
        return supplyAsync(() -> getAsUnchecked(() -> gmail.users().labels().list(ME).execute()))
                .thenApply(listLabelsResponse -> listLabelsResponse.getLabels().stream()
                                                                   .map(InternalGmailLabel::new)
                                                                   .collect(toImmutableList()));
    }

    private CompletableFuture<GmailLabel> createLabel(String labelName) {
        return supplyAsync(() -> getAsUnchecked(() -> gmail.users().labels().create(
                                                                   ME,
                                                                   new Label()
                                                                           .setName(labelName)
                                                                           .setLabelListVisibility("labelShow")
                                                                           .setMessageListVisibility("show"))
                                                           .execute()))
                .thenApply(InternalGmailLabel::new);
    }
}

