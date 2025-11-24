package net.yudichev.jiotty.connector.tesla.fleet;

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
interface BaseClimateState {
    @JsonProperty("is_climate_on")
    boolean climateOn();

    @JsonProperty("driver_temp_setting")
    double driverTempSetting();

    @JsonProperty("inside_temp")
    double insideTemp();

    @JsonDeserialize(converter = EpochMillisecondsConverter.class)
    Instant timestamp();
}
