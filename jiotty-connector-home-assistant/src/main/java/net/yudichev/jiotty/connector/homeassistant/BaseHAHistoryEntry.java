package net.yudichev.jiotty.connector.homeassistant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value.Immutable;

import java.time.Instant;
import java.util.Optional;

@Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseHAHistoryEntry {
    @JsonProperty("entity_id")
    Optional<String> entityId();

    String state();

    @JsonProperty("last_changed")
    Instant lastChanged();
}
