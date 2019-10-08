package net.yudichev.jiotty.connector.google.gmail;

import com.google.api.services.gmail.model.Label;

import static com.google.common.base.Preconditions.checkNotNull;

final class InternalGmailLabel implements GmailLabel {
    private final Label label;

    InternalGmailLabel(Label label) {
        this.label = checkNotNull(label);
    }

    @Override
    public String getName() {
        return label.getName();
    }

    @Override
    public String toString() {
        return label.toString();
    }

    String getId() {
        return label.getId();
    }

}
