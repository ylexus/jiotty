package net.yudichev.jiotty.common.keystore;

import com.google.inject.BindingAnnotation;
import jakarta.inject.Inject;

import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.yudichev.jiotty.common.lang.MoreThrowables.asUnchecked;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;

public final class KeyStoreAccessImpl implements KeyStoreAccess {
    private KeyStore ks;
    private KeyStore.PasswordProtection pp;

    @Inject
    public KeyStoreAccessImpl(@PathToKeystore Path pathToKeystore,
                              @KeyStorePass String keystorePass,
                              @KeyStoreType String keystoreType) {
        char[] keystorePassCharArray = keystorePass.toCharArray();
        asUnchecked(() -> {
            ks = KeyStore.getInstance(keystoreType);
            try (InputStream in = Files.newInputStream(pathToKeystore)) {
                ks.load(in, keystorePassCharArray);
            }
            // All entries use the same password as the keystore here:
            pp = new KeyStore.PasswordProtection(keystorePassCharArray);
        });
    }

    @Override
    public String getEntry(String alias) {
        return getAsUnchecked(() -> {
            var ske = (KeyStore.SecretKeyEntry) ks.getEntry(alias, pp);
            // The stored password is the raw bytes of the SecretKey:
            return new String(ske.getSecretKey().getEncoded(), UTF_8);
        });
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface PathToKeystore {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface KeyStorePass {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface KeyStoreType {
    }
}
