package net.yudichev.jiotty.common.lang;

import com.google.common.collect.Sets;
import net.yudichev.jiotty.common.async.TaskExecutor;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static net.yudichev.jiotty.common.lang.CompositeException.runForAll;

public final class Listeners<T> {
    private final Set<Consumer<? super T>> listeners = Sets.newConcurrentHashSet();

    public Closeable addListener(Consumer<? super T> consumer) {
        checkArgument(listeners.add(consumer), "listener already added: %s", consumer);
        return Closeable.idempotent(() -> listeners.remove(consumer));
    }

    public Closeable addListener(TaskExecutor executor, Supplier<Optional<T>> imageSupplier, Consumer<? super T> consumer) {
        CompletableFuture<Closeable> handleFuture = executor.submit(() -> {
            Closeable result = addListener(consumer);
            imageSupplier.get().ifPresent(consumer);
            return result;
        });
        return Closeable.idempotent(() -> handleFuture.thenAcceptAsync(Closeable::close, executor));
    }

    public void notify(T value) {
        runForAll(listeners, listener -> listener.accept(value));
    }
}
