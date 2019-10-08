package net.yudichev.jiotty.common.inject;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SpecifiedAnnotation {
    private static final SpecifiedAnnotation NO_ANNOTATION = new SpecifiedAnnotation(Key::get);

    private final Function<TypeLiteral<?>, Key<?>> keyFactory;

    private SpecifiedAnnotation(Function<TypeLiteral<?>, Key<?>> keyFactory) {
        this.keyFactory = checkNotNull(keyFactory);
    }

    public static SpecifiedAnnotation forAnnotation(Annotation annotation) {
        return new SpecifiedAnnotation(typeLiteral -> Key.get(typeLiteral, annotation));
    }

    public static SpecifiedAnnotation forAnnotation(Class<? extends Annotation> annotationClass) {
        return new SpecifiedAnnotation(typeLiteral -> Key.get(typeLiteral, annotationClass));
    }

    public static SpecifiedAnnotation forNoAnnotation() {
        return NO_ANNOTATION;
    }

    @SuppressWarnings("unchecked") // guaranteed by method signature
    public <T> Key<T> specify(TypeLiteral<T> typeLiteral) {
        return (Key<T>) keyFactory.apply(typeLiteral);
    }

    public <T> Key<T> specify(Class<T> type) {
        return specify(TypeLiteral.get(type));
    }
}
