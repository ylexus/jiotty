package net.yudichev.jiotty.common.varstore;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryVarStore implements VarStore {
    private final Map<String, Object> valuesByKey = new ConcurrentHashMap<>();

    @Override
    public void saveValue(String key, Object value) {
        valuesByKey.put(key, value);
    }

    @Override
    public <T> Optional<T> readValue(Class<T> type, String key) {
        return Optional.ofNullable(type.cast(valuesByKey.get(key)));
    }
}
