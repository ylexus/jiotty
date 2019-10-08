package net.jiotty.connector.google.gmail;

import net.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface GmailMessage {
    Optional<String> getHeader(String name);

    default String getRequiredHeader(String name) {
        return getHeader(name).orElseThrow(() -> new IllegalStateException("No header '" + name + "' in the message"));
    }

    Collection<GmailMessageAttachment> getAttachments(Predicate<? super String> mimeTypePredicate);

    CompletableFuture<Void> applyLabels(LabelsChange labelsChange);

    CompletableFuture<Void> applyLabels(LabelsChangeNames labelsChange);

    @Value.Immutable
    @PublicImmutablesStyle
    interface BaseLabelsChange {
        List<GmailLabel> getLabelsToAdd();

        List<GmailLabel> getLabelsToRemove();
    }

    @Value.Immutable
    @PublicImmutablesStyle
    interface BaseLabelsChangeNames {
        List<String> getLabelsToAdd();

        List<String> getLabelsToRemove();
    }
}
