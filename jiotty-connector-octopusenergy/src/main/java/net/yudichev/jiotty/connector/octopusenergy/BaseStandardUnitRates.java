package net.yudichev.jiotty.connector.octopusenergy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.util.List;
import java.util.Optional;

@Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseStandardUnitRates {
    @JsonProperty("next")
    Optional<String> nextUrl();

    @Value.Parameter
    @JsonProperty("results")
    List<StandardUnitRate> rates();
}
