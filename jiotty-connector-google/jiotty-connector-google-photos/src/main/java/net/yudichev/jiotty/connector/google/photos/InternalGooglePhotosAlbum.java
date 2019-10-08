package net.yudichev.jiotty.connector.google.photos;

import com.google.common.base.MoreObjects;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.types.proto.Album;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.CompletableFuture.supplyAsync;

final class InternalGooglePhotosAlbum implements GooglePhotosAlbum {
    private static final Logger logger = LoggerFactory.getLogger(InternalGooglePhotosAlbum.class);
    private final PhotosLibraryClient client;
    private final Album album;

    InternalGooglePhotosAlbum(PhotosLibraryClient client, Album album) {
        this.client = checkNotNull(client);
        this.album = checkNotNull(album);
    }

    @Override
    public CompletableFuture<Void> addPhotosByIds(List<String> mediaItemsIds) {
        return supplyAsync(() -> {
            logger.debug("Adding to album {} items {}", album, mediaItemsIds);

            client.batchAddMediaItemsToAlbum(album.getId(), mediaItemsIds);

            logger.debug("Added");
            return null;
        });
    }

    @Override
    public String getTitle() {
        return album.getTitle();
    }

    @Override
    public String getId() {
        return album.getId();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("album", album)
                .toString();
    }
}
