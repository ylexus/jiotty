package net.yudichev.jiotty.connector.world.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.HOURS;
import static net.yudichev.jiotty.common.lang.EvenMoreObjects.mapIfNotNull;

@Value.Immutable
@PublicImmutablesStyle
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseForecastHour {
    @JsonProperty("time_epoch")
    @JsonDeserialize(converter = EpochSecondsConverter.class)
    Instant from();

    @Value.Derived
    default Instant to() {
        return from().plus(1, HOURS);
    }

    @JsonProperty("temp_c")
    double tempCelsius();

    class EpochSecondsConverter extends StdConverter<Long, Instant> {
        @Override
        public Instant convert(Long value) {
            return mapIfNotNull(value, Instant::ofEpochSecond);
        }
    }
}
