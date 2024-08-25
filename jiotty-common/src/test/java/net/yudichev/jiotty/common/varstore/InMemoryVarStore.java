package net.yudichev.jiotty.common.varstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;

public final class InMemoryVarStore implements VarStore {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .registerModule(new GuavaModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Map<String, String> serialisedValuesByKey = new ConcurrentHashMap<>();

    @Override
    public void saveValue(String key, Object value) {
        serialisedValuesByKey.put(key, getAsUnchecked(() -> mapper.writeValueAsString(value)));
    }

    @Override
    public <T> Optional<T> readValue(Class<T> type, String key) {
        return Optional.ofNullable(serialisedValuesByKey.get(key))
                       .map(encodedValue -> getAsUnchecked(() -> mapper.readerFor(type).readValue(encodedValue)));
    }
}
