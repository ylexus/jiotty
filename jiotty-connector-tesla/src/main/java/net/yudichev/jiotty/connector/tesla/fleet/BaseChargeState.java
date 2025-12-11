package net.yudichev.jiotty.connector.tesla.fleet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.io.IOException;

@Value.Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseChargeState {
    @JsonProperty("charging_state")
    @JsonSetter(nulls = Nulls.SKIP)
    @Value.Default
    default TeslaChargingState chargingState() {
        return TeslaChargingState.UNKNOWN;
    }

    @JsonProperty("conn_charge_cable")
    @JsonDeserialize(using = ChargingStateDeserialiser.class)
    boolean chargeCableConnected();

    @JsonProperty("battery_level")
    int batteryLevel();

    @JsonProperty("charge_limit_soc")
    int chargeLimitSoC();

    /**
     * Logic taken from
     * <a href="https://github.com/home-assistant/core/tree/dev/homeassistant/components/tesla_fleet/binary_sensor.py">Home Assistant Tesla Fleet</a>.
     */
    class ChargingStateDeserialiser extends JsonDeserializer<Boolean> {
        @Override
        public Boolean deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            return switch (parser.currentToken()) {
                case VALUE_STRING -> !"<invalid>".equals(parser.getText());
                default -> throw new IllegalArgumentException("conn_charge_cable must be a string but was: " + parser.currentToken());
            };
        }
    }
}
