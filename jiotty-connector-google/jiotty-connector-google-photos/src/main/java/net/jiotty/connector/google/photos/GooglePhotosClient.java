package net.jiotty.connector.google.photos;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // it's quite useful in this case
public interface GooglePhotosClient {
    // TODO document MediaItemCreationFailedException here
    CompletableFuture<GoogleMediaItem> uploadMediaItem(Optional<String> albumId, Path file, Executor executor);

    default CompletableFuture<GoogleMediaItem> uploadMediaItem(Path file, Executor executor) {
        return uploadMediaItem(Optional.empty(), file, executor);
    }

    default CompletableFuture<GoogleMediaItem> uploadMediaItem(Path file) {
        return uploadMediaItem(Optional.empty(), file, ForkJoinPool.commonPool());
    }

    default CompletableFuture<GoogleMediaItem> uploadMediaItem(Optional<String> albumId, Path file) {
        return uploadMediaItem(albumId, file, ForkJoinPool.commonPool());
    }

    CompletableFuture<GooglePhotosAlbum> createAlbum(String name, Executor executor);

    default CompletableFuture<GooglePhotosAlbum> createAlbum(String name) {
        return createAlbum(name, ForkJoinPool.commonPool());
    }

    CompletableFuture<Collection<GooglePhotosAlbum>> listAlbums(Executor executor);

    default CompletableFuture<Collection<GooglePhotosAlbum>> listAlbums() {
        return listAlbums(ForkJoinPool.commonPool());
    }

    CompletableFuture<GooglePhotosAlbum> getAlbum(String albumId, Executor executor);

    default CompletableFuture<GooglePhotosAlbum> getAlbum(String albumId) {
        return getAlbum(albumId, ForkJoinPool.commonPool());
    }
}
