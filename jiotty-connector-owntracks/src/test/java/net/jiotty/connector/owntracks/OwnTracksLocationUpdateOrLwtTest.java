package net.jiotty.connector.owntracks;

import com.google.common.io.Resources;
import net.jiotty.common.lang.Json;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static com.google.common.io.Resources.getResource;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class OwnTracksLocationUpdateOrLwtTest {
    @Test
    void deserialisesLwt() {
        OwnTracksLocationUpdateOrLwt locationUpdateOrLwt = Json.parse("{\"tst\":\"1565543099\",\"_type\":\"lwt\"}", OwnTracksLocationUpdateOrLwt.class);

        assertThat(locationUpdateOrLwt, is(instanceOf(OwnTracksLwt.class)));
    }

    @Test
    void deserialisesLocationUpdate() throws IOException {
        Optional<OwnTrackLocationUpdate> locationUpdate = Json
                .parse(Resources.toString(getResource("location.json"), UTF_8), OwnTracksLocationUpdateOrLwt.class)
                .asLocationUpdate();

        assertThat(locationUpdate, is(optionalWithValue(equalTo(OwnTrackLocationUpdate.builder()
                .setAccuracyMeters(65)
                .setFixTimestampSeconds(1565556840)
                .setLatitude(51.5870361328125)
                .setLongitude(-0.38138078849335633)
                .build()))));
    }
}