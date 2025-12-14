package net.yudichev.jiotty.user.ui;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** DTOs for Displayables to be rendered on the client */
public final class DisplayableDtos {
    private DisplayableDtos() {
    }

    public sealed interface DisplayableDto permits History, ChargingEventsDto, ChargingPricesDto {
        String type();
    }

    /** @param groups history groups by a 'what' key, each with a list of entries (time + text) */
    public record History(Map<String, List<HistoryEntry>> groups) implements DisplayableDto {
        @Override
        @JsonProperty("type")
        public String type() {
            return "history";
        }
    }

    public record HistoryEntry(String time, String text, TextFormat format) {}

    public record ChargingEventsDto(List<ChargingEventDto> events) implements DisplayableDto {
        @Override
        public String type() {
            return "charging_events";
        }
    }

    public record ChargingEventDto(@JsonProperty("t") Instant timestamp,
                                   @JsonProperty("c") double soc,
                                   @JsonProperty("e") String eventType,
                                   @JsonProperty("s") String summary) {}

    public record ChargingPricesDto(List<ChargingPriceDto> prices) implements DisplayableDto {
        @Override
        public String type() {
            return "charging_prices";
        }
    }

    public record ChargingPriceDto(@JsonProperty("t") Instant timestamp,
                                   @JsonProperty("p") double price,
                                   @JsonProperty("a") boolean isActual) {}

}
