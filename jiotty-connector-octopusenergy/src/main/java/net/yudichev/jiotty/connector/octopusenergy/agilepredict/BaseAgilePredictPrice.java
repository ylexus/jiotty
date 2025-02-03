package net.yudichev.jiotty.connector.octopusenergy.agilepredict;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.time.Instant;

@Value.Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseAgilePredictPrice {
    @JsonProperty("date_time")
    Instant dateTime();

    @JsonProperty("agile_pred")
    double predictedPrice();
}
