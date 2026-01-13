package net.yudichev.jiotty.common.lang;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.lang.CompositeException.runForAll;

public final class ConcurrentObservableValue<T> implements ObservableValue<T> {

    private final ConcurrentLinkedQueue<Runnable> actionQueue = new ConcurrentLinkedQueue<>();
    private final AtomicReference<State> state;

    public ConcurrentObservableValue(T initialValue) {
        state = new AtomicReference<>(new State(new LinkedHashSet<>(), initialValue));
    }

    @Override
    public T get() {
        return state.get().value;
    }

    @Override
    public Closeable subscribe(Consumer<? super T> listener) {
        actionQueue.add(() -> state.updateAndGet(s -> s.withListener(listener)));
        flushQueue();
        return Closeable.idempotent(() -> {
            actionQueue.add(() -> state.updateAndGet(s -> s.withoutListener(listener)));
            flushQueue();
        });
    }

    @Override
    public int subscriberCount() {
        return state.get().listeners.size();
    }

    @Override
    public void accept(T value) {
        actionQueue.add(() -> state.updateAndGet(s -> s.withValue(value)));
        flushQueue();
    }

    private void flushQueue() {
        Runnable action;
        while ((action = actionQueue.poll()) != null) {
            action.run();
        }
    }

    @Override
    public String toString() {
        return Objects.toString(get());
    }

    @Override
    public void setNotificationsSuppressed(boolean suppressed) {
        // was not able to implement a satisfyingly threadsafe way of doing this
        throw new UnsupportedOperationException("setNotificationsSuppressed");
    }

    private class State {
        private final LinkedHashSet<Consumer<? super T>> listeners;
        private final T value;

        public State(LinkedHashSet<Consumer<? super T>> listeners, T value) {
            this.listeners = checkNotNull(listeners);
            this.value = value;
        }

        public State withListener(Consumer<? super T> listener) {
            var newListeners = new LinkedHashSet<>(listeners);
            newListeners.add(listener);
            listener.accept(value);
            return new State(newListeners, value);
        }

        public State withoutListener(Consumer<? super T> listener) {
            var newListeners = new LinkedHashSet<>(listeners);
            newListeners.remove(listener);
            return new State(newListeners, value);
        }

        public State withValue(T value) {
            runForAll(listeners, listener -> listener.accept(value));
            return new State(listeners, value);
        }
    }
}
