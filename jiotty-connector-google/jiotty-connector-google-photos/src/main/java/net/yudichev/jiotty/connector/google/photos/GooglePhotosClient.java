package net.yudichev.jiotty.connector.google.photos;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.IntConsumer;

public interface GooglePhotosClient {
    // TODO document MediaItemCreationFailedException here
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    // it's quite useful in this case
    CompletableFuture<GoogleMediaItem> uploadMediaItem(Optional<String> albumId, Path file, Executor executor);

    default CompletableFuture<GoogleMediaItem> uploadMediaItem(Path file, Executor executor) {
        return uploadMediaItem(Optional.empty(), file, executor);
    }

    default CompletableFuture<GoogleMediaItem> uploadMediaItem(Path file) {
        return uploadMediaItem(Optional.empty(), file, ForkJoinPool.commonPool());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // it's quite useful in this case
    default CompletableFuture<GoogleMediaItem> uploadMediaItem(Optional<String> albumId, Path file) {
        return uploadMediaItem(albumId, file, ForkJoinPool.commonPool());
    }

    CompletableFuture<GooglePhotosAlbum> createAlbum(String name, Executor executor);

    default CompletableFuture<GooglePhotosAlbum> createAlbum(String name) {
        return createAlbum(name, ForkJoinPool.commonPool());
    }

    CompletableFuture<List<GooglePhotosAlbum>> listAlbums(IntConsumer loadedAlbumCountProgressCallback, Executor executor);

    default CompletableFuture<List<GooglePhotosAlbum>> listAlbums(Executor executor) {
        return listAlbums(value -> {}, executor);
    }

    default CompletableFuture<List<GooglePhotosAlbum>> listAlbums() {
        return listAlbums(ForkJoinPool.commonPool());
    }

    default CompletableFuture<List<GooglePhotosAlbum>> listAlbums(IntConsumer loadedAlbumCountProgressCallback) {
        return listAlbums(loadedAlbumCountProgressCallback, ForkJoinPool.commonPool());
    }

    CompletableFuture<GooglePhotosAlbum> getAlbum(String albumId, Executor executor);

    default CompletableFuture<GooglePhotosAlbum> getAlbum(String albumId) {
        return getAlbum(albumId, ForkJoinPool.commonPool());
    }
}
