package net.yudichev.jiotty.common.lang;

import org.slf4j.Logger;

import java.util.Collection;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Arrays.asList;
import static net.yudichev.jiotty.common.lang.CompositeException.runForAll;
import static net.yudichev.jiotty.common.lang.MoreThrowables.asUnchecked;

@SuppressWarnings("OverloadedVarargsMethod")
public interface Closeable extends AutoCloseable {
    @SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
    static Closeable forCloseables(Closeable... closeables) {
        return forCloseables(asList(closeables));
    }

    @SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
    static Closeable forCloseables(AutoCloseable... closeables) {
        return forCloseables(asList(closeables));
    }

    static Closeable forActions(Runnable... actions) {
        return forActions(copyOf(actions));
    }

    static Closeable forCloseables(Collection<? extends AutoCloseable> closeables) {
        return forActions(closeables.stream()
                .<Runnable>map(closeable -> ((Closeable) closeable)::close)
                .collect(toImmutableList()));
    }

    static Closeable forActions(Collection<? extends Runnable> actions) {
        return idempotent(() -> runForAll(actions, Runnable::run));
    }

    static Closeable idempotent(AutoCloseable closeable) {
        return new BaseIdempotentCloseable() {
            @Override
            protected void doClose() {
                asUnchecked(closeable::close);
            }
        };
    }

    static Closeable noop() {
        return () -> {};
    }

    static void closeIfNotNull(AutoCloseable closeable) {
        if (closeable != null) {
            asUnchecked(closeable::close);
        }
    }

    static void closeIfNotNull(Closeable... closeable) {
        Stream.of(closeable).forEach(Closeable::closeIfNotNull);
    }

    static void closeIfNotNull(AutoCloseable... closeable) {
        Stream.of(closeable).forEach(Closeable::closeIfNotNull);
    }

    static void closeSafely(AutoCloseable closeable, Logger logger) {
        try {
            asUnchecked(closeable::close);
        } catch (RuntimeException e) {
            logger.warn("Failed to close {}", closeable, e);
        }
    }

    static void closeSafelyIfNotNull(AutoCloseable closeable, Logger logger) {
        if (closeable != null) {
            closeSafely(closeable, logger);
        }
    }

    @Override
    void close();

    default void closeSafely(Logger logger) {
        closeSafely(this, logger);
    }
}
