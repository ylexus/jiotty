package net.yudichev.jiotty.connector.tesla.wallconnector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value.Immutable;


@Immutable
@PublicImmutablesStyle
@JsonSerialize
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseTeslaWallConnectorVitals {
    @JsonProperty("evse_state")
    @JsonDeserialize(converter = EvseStateConverter.class)
    EvseState evsState();

    class EvseStateConverter extends StdConverter<Integer, EvseState> {
        @Override
        public EvseState convert(Integer value) {
            return EvseState.forId(value);
        }
    }
}
