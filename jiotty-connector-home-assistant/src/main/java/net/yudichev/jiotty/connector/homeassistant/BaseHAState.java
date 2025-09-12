package net.yudichev.jiotty.connector.homeassistant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.time.Instant;
import java.util.Optional;

@Value.Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseHAState<A> {
    @JsonProperty("entity_id")
    String entityId();

    String state();

    Optional<A> attributes();

    @JsonProperty("last_changed")
    Instant lastChanged();

    @JsonProperty("last_updated")
    Instant lastUpdated();

    @JsonProperty("last_reported")
    Instant lastReported();
}
