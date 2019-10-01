package net.jiotty.connector.world;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.jiotty.common.lang.PackagePrivateImmutablesStyle;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@PackagePrivateImmutablesStyle
@JsonDeserialize
interface BaseSunriseSunsetResponse {
    String status();

    Optional<SunriseSunsetData> results();
}
