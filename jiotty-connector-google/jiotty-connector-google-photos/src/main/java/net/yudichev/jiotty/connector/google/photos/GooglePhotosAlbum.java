package net.yudichev.jiotty.connector.google.photos;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.google.common.collect.ImmutableList.toImmutableList;

public interface GooglePhotosAlbum {
    CompletableFuture<Void> addPhotosByIds(List<String> mediaItemsIds);

    default CompletableFuture<Void> addPhotos(List<? extends GoogleMediaItem> mediaItems) {
        return addPhotosByIds(mediaItems.stream().map(GoogleMediaItem::getId).collect(toImmutableList()));
    }

    String getTitle();

    String getId();
}
