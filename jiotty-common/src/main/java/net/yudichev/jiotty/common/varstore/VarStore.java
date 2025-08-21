package net.yudichev.jiotty.common.varstore;

import com.google.common.reflect.TypeToken;

import java.util.Optional;

public interface VarStore {
    void saveValue(String key, Object value);

    <T> Optional<T> readValue(TypeToken<T> type, String key);

    default <T> Optional<T> readValue(Class<T> type, String key) {
        return readValue(TypeToken.of(type), key);
    }
}
