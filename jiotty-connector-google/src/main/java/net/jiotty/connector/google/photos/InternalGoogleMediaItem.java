package net.jiotty.connector.google.photos;

import com.google.common.base.MoreObjects;
import com.google.photos.types.proto.MediaItem;

import static com.google.common.base.Preconditions.checkNotNull;

final class InternalGoogleMediaItem implements GoogleMediaItem {
    private final MediaItem mediaItem;

    InternalGoogleMediaItem(MediaItem mediaItem) {
        this.mediaItem = checkNotNull(mediaItem);
    }

    @Override
    public String getId() {
        return mediaItem.getId();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("mediaItem", mediaItem)
                .toString();
    }
}
