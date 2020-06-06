package net.yudichev.jiotty.connector.google.photos;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.IntConsumer;

import static com.google.common.collect.ImmutableList.toImmutableList;

public interface GooglePhotosAlbum {
    String getTitle();

    String getId();

    long getMediaItemCount();

    String getAlbumUrl();

    boolean isWriteable();

    CompletableFuture<Void> addMediaItemsByIds(List<String> mediaItemsIds, Executor executor);

    default CompletableFuture<Void> addMediaItemsByIds(List<String> mediaItemsIds) {
        return addMediaItemsByIds(mediaItemsIds, ForkJoinPool.commonPool());
    }

    default CompletableFuture<Void> addMediaItems(List<? extends GoogleMediaItem> mediaItems, Executor executor) {
        return addMediaItemsByIds(mediaItems.stream().map(GoogleMediaItem::getId).collect(toImmutableList()), executor);
    }

    default CompletableFuture<Void> addMediaItems(List<? extends GoogleMediaItem> mediaItems) {
        return addMediaItems(mediaItems, ForkJoinPool.commonPool());
    }

    CompletableFuture<Void> removeMediaItemsByIds(List<String> mediaItemsIds, Executor executor);

    default CompletableFuture<Void> removeMediaItemsByIds(List<String> mediaItemsIds) {
        return removeMediaItemsByIds(mediaItemsIds, ForkJoinPool.commonPool());
    }

    default CompletableFuture<Void> removeMediaItems(List<? extends GoogleMediaItem> mediaItems, Executor executor) {
        return removeMediaItemsByIds(mediaItems.stream().map(GoogleMediaItem::getId).collect(toImmutableList()), executor);
    }

    default CompletableFuture<Void> removeMediaItems(List<? extends GoogleMediaItem> mediaItems) {
        return removeMediaItems(mediaItems, ForkJoinPool.commonPool());
    }

    CompletableFuture<List<GoogleMediaItem>> getMediaItems(IntConsumer loadedItemCountProgressCallback, Executor executor);

    default CompletableFuture<List<GoogleMediaItem>> getMediaItems(Executor executor) {
        return getMediaItems(command -> { }, executor);
    }

    default CompletableFuture<List<GoogleMediaItem>> getMediaItems() {
        return getMediaItems(ForkJoinPool.commonPool());
    }

    default CompletableFuture<List<GoogleMediaItem>> getMediaItems(IntConsumer loadedItemCountProgressCallback) {
        return getMediaItems(loadedItemCountProgressCallback, ForkJoinPool.commonPool());
    }
}
