package net.yudichev.jiotty.connector.slide;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PackagePrivateImmutablesStyle;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@PackagePrivateImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class BaseSlideResponse<T> {
    public abstract Optional<T> data();

    public abstract Optional<String> error();

    public abstract Optional<String> message();

    public final T dataOrThrow(String errorPrefix) {
        return data().orElseThrow(() -> new RuntimeException(errorPrefix + ": " + error().orElseGet(() -> message().orElse("no error information"))));
    }
}
