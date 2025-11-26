package net.yudichev.jiotty.common.net;

import com.google.inject.BindingAnnotation;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;

final class SslCustomisationProvider implements Provider<SslCustomisation> {
    private final Path trustStorePath;
    private final String trustStorePassword;

    @Inject
    public SslCustomisationProvider(@Dependency Path trustStorePath, @Dependency String trustStorePassword) {
        this.trustStorePath = checkNotNull(trustStorePath);
        this.trustStorePassword = checkNotNull(trustStorePassword);
    }

    @Override
    public SslCustomisation get() {
        return getAsUnchecked(() -> {
            try (InputStream in = Files.newInputStream(trustStorePath)) {
                return SslCustomisation.fromTrustStore(in, trustStorePassword);
            }
        });
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }

}
