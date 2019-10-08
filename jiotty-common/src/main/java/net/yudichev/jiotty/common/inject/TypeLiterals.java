package net.yudichev.jiotty.common.inject;

import com.google.common.reflect.TypeToken;
import com.google.inject.TypeLiteral;

public final class TypeLiterals {
    private TypeLiterals() {
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeLiteral<T> asTypeLiteral(TypeToken<T> typeToken) {
        return (TypeLiteral<T>) TypeLiteral.get(typeToken.getType());
    }
}
