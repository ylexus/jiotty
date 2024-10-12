package net.yudichev.jiotty.connector.octopusenergy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.time.Instant;

@Value.Immutable
@PublicImmutablesStyle
@JsonDeserialize
interface BaseTariff {
    @JsonProperty("tariff_code")
    String tariffCode();

    @JsonProperty("valid_from")
    Instant validFrom();

    @JsonProperty("valid_to")
    Instant validTo();
}
