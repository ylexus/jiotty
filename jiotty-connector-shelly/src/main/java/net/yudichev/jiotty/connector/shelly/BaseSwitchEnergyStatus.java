package net.yudichev.jiotty.connector.shelly;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.util.List;

@Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseSwitchEnergyStatus {
    @Value.Parameter
    @JsonProperty("minute_ts")
    long endOfNewestMinuteEpochTimeSec();

    @Value.Parameter
    @JsonProperty("by_minute")
    List<Double> mWHoursByMinute();
}
