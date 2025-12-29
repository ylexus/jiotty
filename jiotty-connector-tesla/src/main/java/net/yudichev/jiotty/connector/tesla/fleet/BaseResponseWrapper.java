package net.yudichev.jiotty.connector.tesla.fleet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.Either;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseResponseWrapper<R> {
    Optional<R> response();

    Optional<String> error();

    @Value.Derived
    @Value.Auxiliary
    default Either<R, String> responseOrError() {
        return response().<Either<R, String>>map(Either::left).orElseGet(() -> Either.right(error().orElse("unknown error")));
    }
}
