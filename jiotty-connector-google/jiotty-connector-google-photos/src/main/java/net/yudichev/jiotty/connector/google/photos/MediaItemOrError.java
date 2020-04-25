package net.yudichev.jiotty.connector.google.photos;

import com.google.common.base.MoreObjects;
import com.google.rpc.Status;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class MediaItemOrError {
    protected MediaItemOrError() {
    }

    public static MediaItemOrError item(GoogleMediaItem item) {
        return new MediaItem(item);
    }

    public static MediaItemOrError error(Status errorStatus) {
        return new MediaItemError(errorStatus);
    }

    public abstract void accept(Consumer<? super GoogleMediaItem> itemConsumer,
                                Consumer<? super Status> errorConsumer);

    public abstract <U> U map(Function<? super GoogleMediaItem, ? extends U> itemMapper,
                              Function<? super Status, ? extends U> errorMapper);

    public abstract Optional<GoogleMediaItem> item();

    public abstract Optional<Status> errorStatus();

    private static final class MediaItem extends MediaItemOrError {
        private final GoogleMediaItem item;

        private MediaItem(GoogleMediaItem item) {
            this.item = checkNotNull(item);
        }

        @Override
        public void accept(Consumer<? super GoogleMediaItem> itemConsumer,
                           Consumer<? super Status> errorConsumer) {
            itemConsumer.accept(item);
        }

        @Override
        public <U> U map(Function<? super GoogleMediaItem, ? extends U> itemMapper,
                         Function<? super Status, ? extends U> errorMapper) {
            return itemMapper.apply(item);
        }

        @Override
        public Optional<GoogleMediaItem> item() {
            return Optional.of(item);
        }

        @Override
        public Optional<Status> errorStatus() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("item", item)
                    .toString();
        }
    }

    private static final class MediaItemError extends MediaItemOrError {
        private final Status status;

        private MediaItemError(Status status) {
            this.status = checkNotNull(status);
        }

        @Override
        public void accept(Consumer<? super GoogleMediaItem> itemConsumer,
                           Consumer<? super Status> errorConsumer) {
            errorConsumer.accept(status);
        }

        @Override
        public <U> U map(Function<? super GoogleMediaItem, ? extends U> itemMapper,
                         Function<? super Status, ? extends U> errorMapper) {
            return errorMapper.apply(status);
        }

        @Override
        public Optional<GoogleMediaItem> item() {
            return Optional.empty();
        }

        @Override
        public Optional<Status> errorStatus() {
            return Optional.of(status);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("status", status)
                    .toString();
        }
    }
}
