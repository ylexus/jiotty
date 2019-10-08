package net.yudichev.jiotty.common.lang;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class Json {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .registerModule(new GuavaModule());

    private Json() {
    }

    public static JsonNode parse(String json) {
        return MoreThrowables.getAsUnchecked(() -> mapper.readTree(json));
    }

    public static <T> T parse(String json, Class<T> type) {
        return MoreThrowables.getAsUnchecked(() -> mapper.readValue(json, type));
    }

    public static ObjectNode object() {
        return mapper.createObjectNode();
    }

    public static String stringify(Object value) {
        return MoreThrowables.getAsUnchecked(() -> mapper.writeValueAsString(value));
    }
}
