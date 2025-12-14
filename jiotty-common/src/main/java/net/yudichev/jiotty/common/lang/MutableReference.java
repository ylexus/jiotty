package net.yudichev.jiotty.common.lang;

public final class MutableReference<T> {
    private T value;

    public MutableReference() {
    }

    public MutableReference(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
