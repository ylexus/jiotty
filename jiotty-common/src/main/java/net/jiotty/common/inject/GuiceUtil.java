package net.jiotty.common.inject;

import com.google.inject.name.Names;

import java.lang.annotation.Annotation;
import java.util.UUID;

public final class GuiceUtil {
    private GuiceUtil() {
    }

    public static Annotation uniqueAnnotation() {
        return Names.named(UUID.randomUUID().toString());
    }
}
