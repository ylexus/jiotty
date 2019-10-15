package net.yudichev.jiotty.common.lang;

import net.yudichev.jiotty.common.async.Scheduler;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class StabilisingConsumer<T> implements Consumer<T> {
    private final Scheduler scheduler;
    private final Duration stabilisationDuration;
    private final Consumer<T> delegate;

    private final AtomicReference<Closeable> timerSchedule = new AtomicReference<>();
    private volatile T pendingValue;

    public StabilisingConsumer(Scheduler scheduler, Duration stabilisationDuration, Consumer<T> delegate) {
        this.scheduler = checkNotNull(scheduler);
        checkArgument(!stabilisationDuration.isNegative(), "stabilisationDuration must not be negative, but was %s", stabilisationDuration);
        this.stabilisationDuration = stabilisationDuration;
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public void accept(T t) {
        pendingValue = t;
        Closeable.closeIfNotNull(timerSchedule.getAndSet(scheduler.schedule(stabilisationDuration, this::onStabilisationTimer)));
    }

    private void onStabilisationTimer() {
        delegate.accept(pendingValue);
    }
}
