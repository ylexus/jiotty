package net.yudichev.jiotty.connector.miele;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.time.Duration;
import java.util.Optional;

@Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseDeviceState extends MieleEvent {
    @JsonProperty("ProgramID")
    StateValue program();

    StateValue status();

    @JsonProperty("remainingTime")
    Optional<int[]> remainingTimeParts();

    @JsonIgnore
    @Value.Derived
    default Optional<Duration> remainingTime() {
        return remainingTimeParts().map(hm -> hm.length == 2 ? Duration.ofHours(hm[0]).plusMinutes(hm[1]) : null);
    }

    @JsonProperty("remoteEnable")
    RemoteControlStatus remoteControlStatus();
}
