package net.yudichev.jiotty.common.lang;

import java.util.Objects;

public interface EqualityComparator<T> {
    boolean areEqual(T left, T right);

    static <T> EqualityComparator<T> referenceEquality() {
        return (left, right) -> left == right;
    }

    static <T> EqualityComparator<T> equality() {
        return Objects::equals;
    }
}
