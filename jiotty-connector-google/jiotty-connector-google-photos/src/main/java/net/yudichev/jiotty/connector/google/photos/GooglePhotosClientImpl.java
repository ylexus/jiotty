package net.yudichev.jiotty.connector.google.photos;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.common.collect.ImmutableList;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.library.v1.proto.ListAlbumsRequest;
import com.google.photos.library.v1.proto.NewMediaItemResult;
import com.google.photos.library.v1.upload.UploadMediaItemRequest;
import com.google.photos.library.v1.upload.UploadMediaItemResponse;
import com.google.photos.types.proto.Album;
import com.google.rpc.Code;
import com.google.rpc.Status;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.Closeable;
import net.yudichev.jiotty.connector.google.common.GoogleAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.IntConsumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static net.yudichev.jiotty.common.lang.Closeable.closeIfNotNull;
import static net.yudichev.jiotty.common.lang.Closeable.idempotent;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static net.yudichev.jiotty.connector.google.common.impl.Bindings.Authorization;

final class GooglePhotosClientImpl extends BaseLifecycleComponent implements GooglePhotosClient {
    private static final Logger logger = LoggerFactory.getLogger(GooglePhotosClientImpl.class);
    private final Provider<GoogleAuthorization> googleAuthorizationProvider;
    private PhotosLibraryClient client;
    private Closeable closeable;

    @Inject
    GooglePhotosClientImpl(@Authorization Provider<GoogleAuthorization> googleAuthorizationProvider) {
        this.googleAuthorizationProvider = checkNotNull(googleAuthorizationProvider);
    }

    @Override
    public CompletableFuture<String> uploadMediaData(Path file, Executor executor) {
        PhotosLibraryClient theClient = whenStartedAndNotLifecycling(() -> client);
        return supplyAsync(() -> whenStartedAndNotLifecycling(() -> {
            logger.debug("Started uploading {}", file);
            UploadMediaItemResponse uploadResponse;
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file.toFile(), "r")) {
                UploadMediaItemRequest uploadRequest = UploadMediaItemRequest.newBuilder()
                        .setDataFile(randomAccessFile)
                        .setMimeType("application/octet-stream")
                        .build();
                uploadResponse = theClient.uploadMediaItem(uploadRequest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            uploadResponse.getError().ifPresent(error -> {throw new RuntimeException("Failed uploading " + file, error.getCause());});
            // guaranteed
            @SuppressWarnings("OptionalGetWithoutIsPresent")
            String uploadToken = uploadResponse.getUploadToken().get();
            logger.debug("Uploaded file {}, upload token {}", file, uploadToken);
            return uploadToken;
        }), executor);
    }

    @Override
    public CompletableFuture<List<MediaItemOrError>> createMediaItems(Optional<String> albumId,
                                                                      List<NewMediaItem> newMediaItems,
                                                                      Executor executor) {
        PhotosLibraryClient theClient = whenStartedAndNotLifecycling(() -> client);
        return supplyAsync(() -> whenStartedAndNotLifecycling(() -> {
            logger.debug("Create media items in album {}, items {}", albumId, newMediaItems);
            ImmutableList<com.google.photos.library.v1.proto.NewMediaItem> newItems = newMediaItems.stream()
                    .map(BaseNewMediaItem::asGoogleMediaItem)
                    .collect(toImmutableList());
            List<NewMediaItemResult> newMediaItemResultsList = albumId
                    .map(theAlbumId -> theClient.batchCreateMediaItems(theAlbumId, newItems))
                    .orElseGet(() -> theClient.batchCreateMediaItems(newItems))
                    .getNewMediaItemResultsList();
            checkState(newMediaItemResultsList.size() == newItems.size(),
                    "expected media item creation result list size %s, got: %s", newItems.size(), newMediaItemResultsList);

            return newMediaItemResultsList.stream()
                    .map(newMediaItemResult -> {
                        Status status = newMediaItemResult.getStatus();
                        MediaItemOrError mediaItemOrError = status.getCode() == Code.OK_VALUE ?
                                MediaItemOrError.item(new InternalGoogleMediaItem(newMediaItemResult.getMediaItem())) :
                                MediaItemOrError.error(status);
                        logger.debug("Finished uploading token {}, result {}", newMediaItemResult.getUploadToken(), newMediaItemResult);
                        return mediaItemOrError;
                    })
                    .collect(toImmutableList());
        }), executor);
    }

    @Override
    public CompletableFuture<GooglePhotosAlbum> createAlbum(String name, Executor executor) {
        PhotosLibraryClient theClient = whenStartedAndNotLifecycling(() -> client);
        return supplyAsync(() -> {
            logger.debug("Creating album '{}'", name);
            Album album = theClient.createAlbum(name);
            logger.debug("Created album {}", album);
            return new InternalGooglePhotosAlbum(theClient, album);
        }, executor);
    }

    @Override
    public CompletableFuture<List<GooglePhotosAlbum>> listAlbums(IntConsumer loadedAlbumCountProgressCallback, Executor executor) {
        PhotosLibraryClient theClient = whenStartedAndNotLifecycling(() -> client);
        return supplyAsync(() -> {
            logger.debug("List all albums");
            PagedRequest<Album> request = new PagedRequest<>(logger, loadedAlbumCountProgressCallback, pageToken -> {
                ListAlbumsRequest.Builder requestBuilder = ListAlbumsRequest.newBuilder()
                        .setExcludeNonAppCreatedData(false)
                        .setPageSize(50);
                pageToken.ifPresent(requestBuilder::setPageToken);
                return theClient.listAlbums(requestBuilder.build());
            });
            List<GooglePhotosAlbum> result = request.getAll().map(album -> new InternalGooglePhotosAlbum(theClient, album))
                    .collect(toImmutableList());
            logger.debug("Listed {} album(s)", result.size());
            return result;
        }, executor);
    }

    @Override
    public CompletableFuture<GooglePhotosAlbum> getAlbum(String albumId, Executor executor) {
        PhotosLibraryClient theClient = whenStartedAndNotLifecycling(() -> client);
        return supplyAsync(() -> {
            logger.debug("Get album info for albumId {}", albumId);
            Album album = theClient.getAlbum(albumId);
            logger.debug("Got album info for albumId {}: {}", albumId, album);
            return new InternalGooglePhotosAlbum(theClient, album);
        }, executor);
    }

    @Override
    protected void doStart() {
        //noinspection resource it's closed
        client = getAsUnchecked(() -> PhotosLibraryClient.initialize(PhotosLibrarySettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(googleAuthorizationProvider.get().getCredentials()))
                .build()));
        closeable = idempotent(() -> client.close());
    }

    @Override
    protected void doStop() {
        closeIfNotNull(closeable);
    }
}
