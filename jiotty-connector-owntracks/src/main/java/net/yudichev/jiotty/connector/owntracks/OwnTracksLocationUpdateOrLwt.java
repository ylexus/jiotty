package net.yudichev.jiotty.connector.owntracks;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Optional;

@SuppressWarnings("ClassReferencesSubclass") // by design
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OwnTrackLocationUpdate.class, name = "location"),
        @JsonSubTypes.Type(value = OwnTracksLwt.class, name = "lwt")
})
interface OwnTracksLocationUpdateOrLwt {
    default Optional<OwnTrackLocationUpdate> asLocationUpdate() {
        //noinspection InstanceofThis by design
        return this instanceof OwnTrackLocationUpdate ? Optional.of((OwnTrackLocationUpdate) this) : Optional.empty();
    }
}
