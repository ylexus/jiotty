package net.yudichev.jiotty.common.lang;

public interface HasName {
    default String name() {
        return String.format("%s @ %s", getClass().getSimpleName(), System.identityHashCode(this));
    }
}
