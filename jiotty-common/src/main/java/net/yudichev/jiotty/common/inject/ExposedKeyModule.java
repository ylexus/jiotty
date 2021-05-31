package net.yudichev.jiotty.common.inject;

import com.google.common.reflect.TypeToken;
import com.google.inject.Key;
import com.google.inject.Module;

import static net.yudichev.jiotty.common.inject.TypeLiterals.asTypeLiteral;

public interface ExposedKeyModule<T> extends Module {
    default Key<T> getExposedKey() {
        return Key.get(asTypeLiteral(new TypeToken<>(getClass()) {}));
    }
}
