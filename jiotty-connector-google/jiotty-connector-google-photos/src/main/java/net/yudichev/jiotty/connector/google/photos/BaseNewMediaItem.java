package net.yudichev.jiotty.connector.google.photos;

import com.google.photos.library.v1.proto.NewMediaItem;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.util.Optional;

import static com.google.photos.library.v1.util.NewMediaItemFactory.createNewMediaItem;

@Immutable
@PublicImmutablesStyle
abstract class BaseNewMediaItem {
    @Value.Parameter
    public abstract String uploadToken();

    @Value.Parameter
    public abstract Optional<String> description();

    final NewMediaItem asGoogleMediaItem() {
        return description().map(theDescription -> createNewMediaItem(uploadToken(), theDescription)).orElseGet(() -> createNewMediaItem(uploadToken()));
    }
}
