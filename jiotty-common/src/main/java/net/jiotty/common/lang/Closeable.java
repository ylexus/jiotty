package net.jiotty.common.lang;

import java.util.Collection;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Arrays.asList;
import static net.jiotty.common.lang.CompositeException.runForAll;

@SuppressWarnings("OverloadedVarargsMethod")
public interface Closeable {
    void close();

    static Closeable forCloseables(Closeable... closeables) {
        return forCloseables(asList(closeables));
    }

    static Closeable forActions(Runnable... actions) {
        return forActions(copyOf(actions));
    }

    static Closeable forCloseables(Collection<? extends Closeable> closeables) {
        return forActions(closeables.stream()
                .<Runnable>map(closeable -> ((Closeable) closeable)::close)
                .collect(toImmutableList()));
    }

    static Closeable forActions(Collection<? extends Runnable> actions) {
        return idempotent(() -> runForAll(actions, Runnable::run));
    }

    static Closeable idempotent(Closeable closeable) {
        return new BaseIdempotentCloseable() {
            @Override
            protected void doClose() {
                closeable.close();
            }
        };
    }

    static Closeable noop() {
        return () -> {};
    }

    static void closeIfNotNull(Closeable closeable) {
        if (closeable != null) {
            closeable.close();
        }
    }

    static void closeIfNotNull(Closeable... closeable) {
        Stream.of(closeable).forEach(Closeable::closeIfNotNull);
    }
}
