package net.jiotty.connector.owntracks;

import net.jiotty.common.lang.Closeable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

public interface OwnTracks {
    Closeable subscribeToTransitions(Consumer<OwnTracksUpdate<OwnTracksTransition>> handler, Executor executor);

    default Closeable subscribeToTransitions(Consumer<OwnTracksUpdate<OwnTracksTransition>> handler) {
        return subscribeToTransitions(handler, directExecutor());
    }

    Closeable subscribeToLocationUpdates(Consumer<OwnTracksUpdate<OwnTrackLocationUpdate>> handler, Executor executor);

    default Closeable subscribeToLocationUpdates(Consumer<OwnTracksUpdate<OwnTrackLocationUpdate>> handler) {
        return subscribeToLocationUpdates(handler, directExecutor());
    }

    CompletableFuture<Void> publishLocationUpdateRequest(DeviceKey deviceKey);
}
