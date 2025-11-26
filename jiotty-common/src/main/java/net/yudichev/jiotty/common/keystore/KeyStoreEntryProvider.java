package net.yudichev.jiotty.common.keystore;

import com.google.inject.BindingAnnotation;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

final class KeyStoreEntryProvider implements Provider<String> {
    private final String alias;
    private final KeyStoreAccess keyStoreAccess;

    @Inject
    public KeyStoreEntryProvider(@Alias String alias,
                                 KeyStoreAccess keyStoreAccess) {
        this.alias = checkNotNull(alias);
        this.keyStoreAccess = checkNotNull(keyStoreAccess);
    }

    @Override
    public String get() {
        return keyStoreAccess.getEntry(alias);
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Alias {
    }
}
