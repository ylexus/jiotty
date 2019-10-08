package net.yudichev.jiotty.connector.google.photos;

import com.google.rpc.Status;

import static com.google.common.base.Preconditions.checkNotNull;

public final class MediaItemCreationFailedException extends RuntimeException {
    private final Status status;

    // public for tests
    @SuppressWarnings("WeakerAccess")
    public MediaItemCreationFailedException(String message, Status status) {
        super(message);
        this.status = checkNotNull(status);
    }

    public Status getStatus() {
        return status;
    }
}
