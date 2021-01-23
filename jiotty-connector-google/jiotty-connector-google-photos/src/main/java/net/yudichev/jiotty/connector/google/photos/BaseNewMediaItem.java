package net.yudichev.jiotty.connector.google.photos;

import com.google.photos.library.v1.proto.NewMediaItem;
import com.google.photos.library.v1.proto.SimpleMediaItem;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.util.Optional;

@Immutable
@PublicImmutablesStyle
abstract class BaseNewMediaItem {
    @Value.Parameter
    public abstract String uploadToken();

    @Value.Parameter
    public abstract Optional<String> fileName();

    @Value.Parameter
    @Value.Default
    public Optional<String> description() {
        return fileName();
    }

    final NewMediaItem asGoogleMediaItem() {
        SimpleMediaItem.Builder simpleMediaItemBuilder = SimpleMediaItem.newBuilder()
                .setUploadToken(uploadToken());
        fileName().ifPresent(simpleMediaItemBuilder::setFileName);
        NewMediaItem.Builder newMediaItemBuilder = NewMediaItem.newBuilder()
                .setSimpleMediaItem(simpleMediaItemBuilder);
        description().ifPresent(newMediaItemBuilder::setDescription);
        return newMediaItemBuilder.build();
    }
}
