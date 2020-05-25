package net.yudichev.jiotty.connector.google.photos;

import java.time.Instant;

public interface GoogleMediaItem {
    String getId();

    Instant getCreationTime();
}
