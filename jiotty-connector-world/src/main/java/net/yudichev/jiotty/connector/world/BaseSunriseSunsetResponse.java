package net.yudichev.jiotty.connector.world;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PackagePrivateImmutablesStyle;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@PackagePrivateImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseSunriseSunsetResponse {
    String status();

    Optional<SunriseSunsetData> results();
}
