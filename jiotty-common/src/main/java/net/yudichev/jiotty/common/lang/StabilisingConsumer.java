package net.yudichev.jiotty.common.lang;

import net.yudichev.jiotty.common.async.Scheduler;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.lang.Closeable.closeIfNotNull;

/// Delays propagating updates until no more updates are received within `stabilisationDuration`, unless `ignoreStabilisationPredicate` returns true, in which
/// case the value is immediately propagated.
public final class StabilisingConsumer<T> implements Consumer<T> {
    private final Scheduler scheduler;
    private final Predicate<T> ignoreStabilisationPredicate;
    private final Duration stabilisationDuration;
    private final Consumer<T> delegate;

    private final AtomicReference<Closeable> timerSchedule = new AtomicReference<>();
    private volatile T pendingValue;

    public StabilisingConsumer(Scheduler scheduler, Duration stabilisationDuration, Consumer<T> delegate) {
        this(scheduler, stabilisationDuration, delegate, _ -> false);
    }

    public StabilisingConsumer(Scheduler scheduler, Duration stabilisationDuration, Consumer<T> delegate, Predicate<T> ignoreStabilisationPredicate) {
        this.scheduler = checkNotNull(scheduler);
        this.ignoreStabilisationPredicate = checkNotNull(ignoreStabilisationPredicate);
        checkArgument(!stabilisationDuration.isNegative(), "stabilisationDuration must not be negative, but was %s", stabilisationDuration);
        this.stabilisationDuration = stabilisationDuration;
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public void accept(T t) {
        pendingValue = t;
        if (ignoreStabilisationPredicate.test(t)) {
            closeIfNotNull(timerSchedule.getAndSet(null));
            delegate.accept(t);
        } else {
            closeIfNotNull(timerSchedule.getAndSet(null));
            timerSchedule.set(scheduler.schedule(stabilisationDuration, this::onStabilisationTimer));
        }
    }

    private void onStabilisationTimer() {
        delegate.accept(pendingValue);
    }
}
