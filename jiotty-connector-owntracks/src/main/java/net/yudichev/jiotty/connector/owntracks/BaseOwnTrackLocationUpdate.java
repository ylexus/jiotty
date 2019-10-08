package net.yudichev.jiotty.connector.owntracks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;

@Value.Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseOwnTrackLocationUpdate extends OwnTracksLocationUpdateOrLwt, HasFixTimestamp {
    @JsonProperty("lon")
    double longitude();

    @JsonProperty("lat")
    double latitude();

    @JsonProperty("acc")
    Optional<Integer> accuracyMeters();

    // TODO verify
    @JsonProperty("inregions")
    Optional<Set<String>> inRegions();
}
