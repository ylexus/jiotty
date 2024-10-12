package net.yudichev.jiotty.connector.octopusenergy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value.Immutable;

import java.time.Instant;

@Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseStandardUnitRate {
    @JsonProperty("value_exc_vat")
    double valueExcVat();

    @JsonProperty("value_inc_vat")
    double valueIncVat();

    @JsonProperty("valid_from")
    Instant validFrom();

    @JsonProperty("valid_to")
    Instant validTo();
}
