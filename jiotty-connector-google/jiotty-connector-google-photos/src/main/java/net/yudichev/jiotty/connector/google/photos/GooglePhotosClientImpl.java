package net.yudichev.jiotty.connector.google.photos;

import com.google.inject.BindingAnnotation;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.library.v1.proto.ListAlbumsRequest;
import com.google.photos.library.v1.proto.NewMediaItem;
import com.google.photos.library.v1.proto.NewMediaItemResult;
import com.google.photos.library.v1.upload.UploadMediaItemRequest;
import com.google.photos.library.v1.upload.UploadMediaItemResponse;
import com.google.photos.library.v1.util.NewMediaItemFactory;
import com.google.photos.types.proto.Album;
import com.google.rpc.Code;
import com.google.rpc.Status;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.Closeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static net.yudichev.jiotty.common.lang.Closeable.closeIfNotNull;
import static net.yudichev.jiotty.common.lang.Closeable.idempotent;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;

final class GooglePhotosClientImpl extends BaseLifecycleComponent implements GooglePhotosClient {
    private static final Logger logger = LoggerFactory.getLogger(GooglePhotosClientImpl.class);
    private final PhotosLibrarySettings photosLibrarySettings;
    private PhotosLibraryClient client;
    private Closeable closeable;

    @Inject
    GooglePhotosClientImpl(@Dependency PhotosLibrarySettings photosLibrarySettings) {
        this.photosLibrarySettings = checkNotNull(photosLibrarySettings);
    }

    @Override
    public CompletableFuture<GoogleMediaItem> uploadMediaItem(Optional<String> albumId, Path file, Executor executor) {
        return supplyAsync(() -> {
            logger.debug("Started uploading {}", file);
            String fileName = file.getFileName().toString();
            UploadMediaItemResponse uploadResponse;
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file.toFile(), "r")) {
                UploadMediaItemRequest uploadRequest = UploadMediaItemRequest.newBuilder()
                        .setFileName(fileName)
                        .setDataFile(randomAccessFile)
                        .build();
                uploadResponse = client.uploadMediaItem(uploadRequest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            uploadResponse.getError().ifPresent(error -> {throw new RuntimeException("Failed uploading data", error.getCause());});
            // guaranteed
            @SuppressWarnings("OptionalGetWithoutIsPresent")
            String uploadToken = uploadResponse.getUploadToken().get();
            logger.debug("Uploaded file {}, upload token {}", file, uploadToken);

            NewMediaItem newMediaItem = NewMediaItemFactory
                    .createNewMediaItem(uploadToken, fileName);
            List<NewMediaItem> newItems = of(newMediaItem);
            List<NewMediaItemResult> newMediaItemResultsList = albumId
                    .map(theAlbumId -> client.batchCreateMediaItems(theAlbumId, newItems))
                    .orElseGet(() -> client.batchCreateMediaItems(newItems)).getNewMediaItemResultsList();
            checkState(newMediaItemResultsList.size() == 1,
                    "expected media item creation result list size 1, got: %s", newMediaItemResultsList);
            NewMediaItemResult newMediaItemResult = newMediaItemResultsList.get(0);
            Status status = newMediaItemResult.getStatus();
            if (status.getCode() == Code.OK_VALUE) {
                logger.debug("Finished uploading {}, media item {}", file, newMediaItemResult.getMediaItem());
                return new InternalGoogleMediaItem(newMediaItemResult.getMediaItem());
            } else {
                throw new MediaItemCreationFailedException(String.format("Unable to create media item for file %s status %s: %s",
                        file, status.getCode(), status.getMessage()), status);
            }

        }, executor);
    }

    @Override
    public CompletableFuture<GooglePhotosAlbum> createAlbum(String name, Executor executor) {
        return supplyAsync(() -> {
            logger.debug("Creating album '{}'", name);
            Album album = client.createAlbum(name);
            logger.debug("Created album {}", album);
            return new InternalGooglePhotosAlbum(client, album);
        }, executor);
    }

    @Override
    public CompletableFuture<List<GooglePhotosAlbum>> listAlbums(Executor executor) {
        return supplyAsync(() -> {
                    logger.debug("List all albums");
                    PagedRequest<Album> request = new PagedRequest<>(logger, pageToken -> {
                        ListAlbumsRequest.Builder requestBuilder = ListAlbumsRequest.newBuilder()
                                .setExcludeNonAppCreatedData(false)
                                .setPageSize(50);
                        pageToken.ifPresent(requestBuilder::setPageToken);
                        return client.listAlbums(requestBuilder.build());
                    });
                    List<GooglePhotosAlbum> result = request.getAll().map(album -> new InternalGooglePhotosAlbum(client, album))
                            .collect(toImmutableList());
                    logger.debug("Listed {} album(s)", result.size());
                    return result;
                },
                executor);
    }

    @Override
    public CompletableFuture<GooglePhotosAlbum> getAlbum(String albumId, Executor executor) {
        return supplyAsync(() -> {
            logger.debug("Get album info for albumId {}", albumId);
            Album album = client.getAlbum(albumId);
            logger.debug("Got album info for albumId {}: {}", albumId, album);
            return new InternalGooglePhotosAlbum(client, album);
        }, executor);
    }

    @Override
    protected void doStart() {
        //noinspection resource it's closed
        client = getAsUnchecked(() -> PhotosLibraryClient.initialize(photosLibrarySettings));
        closeable = idempotent(() -> client.close());
    }

    @Override
    protected void doStop() {
        closeIfNotNull(closeable);
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }
}
