package net.yudichev.jiotty.common.varstore;

import java.util.Optional;

public interface VarStore {
    void saveValue(String key, Object value);

    <T> Optional<T> readValue(Class<T> type, String key);
}
