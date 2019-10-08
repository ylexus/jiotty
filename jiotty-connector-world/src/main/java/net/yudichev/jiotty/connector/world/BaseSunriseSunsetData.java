package net.yudichev.jiotty.connector.world;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PackagePrivateImmutablesStyle;
import org.immutables.value.Value;

import java.time.Instant;

@Value.Immutable
@PackagePrivateImmutablesStyle
@JsonDeserialize
public interface BaseSunriseSunsetData {
    Instant sunrise();

    Instant sunset();

    @JsonProperty("solar_noon")
    Instant solarNoon();

    @JsonProperty("day_length")
    int dayLengthSeconds();

    @JsonProperty("civil_twilight_begin")
    Instant civilTwilightBegin();

    @JsonProperty("civil_twilight_end")
    Instant civilTwilightEnd();

    @JsonProperty("nautical_twilight_begin")
    Instant nauticalTwilightBegin();

    @JsonProperty("nautical_twilight_end")
    Instant nauticalTwilightEnd();

    @JsonProperty("astronomical_twilight_begin")
    Instant astronomicalTwilightBegin();

    @JsonProperty("astronomical_twilight_end")
    Instant astronomicalTwilightEnd();
}
