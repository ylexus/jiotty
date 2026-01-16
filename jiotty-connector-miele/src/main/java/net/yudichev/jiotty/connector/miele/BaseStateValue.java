package net.yudichev.jiotty.connector.miele;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseStateValue extends MieleEvent {
    @Value.Parameter
    @JsonProperty("value_raw")
    int id();

    /// @throws IllegalArgumentException if this state value does not represent an appliance status
    default MieleStatus asStatus() {
        return MieleStatus.forId(id());
    }

    @Value.Parameter
    @JsonProperty("value_localized")
    String name();
}
