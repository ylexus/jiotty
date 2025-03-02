package net.yudichev.jiotty.connector.miele;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseMieleActions extends MieleEvent {
    @JsonProperty("powerOn")
    boolean powerOnAvailable();

    @JsonProperty("powerOff")
    boolean powerOffAvailable();
}
