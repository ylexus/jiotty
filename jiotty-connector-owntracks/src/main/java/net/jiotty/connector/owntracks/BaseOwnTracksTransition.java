package net.jiotty.connector.owntracks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseOwnTracksTransition extends HasFixTimestamp {
    @JsonProperty("lat")
    double latitude();

    @JsonProperty("lon")
    double longitude();

    @JsonProperty("acc")
    Optional<Integer> accuracyMeters();

    @JsonProperty("tid")
    Optional<String> trackerId();

    @JsonProperty("event")
    String event();

    @JsonProperty("desc")
    String waypointName();

    @JsonProperty("t")
    Optional<String> trigger();
}
