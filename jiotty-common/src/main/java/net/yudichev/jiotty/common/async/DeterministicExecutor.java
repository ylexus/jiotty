package net.yudichev.jiotty.common.async;

import net.yudichev.jiotty.common.lang.BaseIdempotentCloseable;
import net.yudichev.jiotty.common.lang.Closeable;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

final class DeterministicExecutor extends BaseIdempotentCloseable implements SchedulingExecutor {
    private final ProgrammableClock clock;
    private final String threadNameBase;
    private boolean closed;

    DeterministicExecutor(ProgrammableClock clock, String threadNameBase) {
        this.clock = checkNotNull(clock);
        this.threadNameBase = checkNotNull(threadNameBase);
    }

    @Override
    public void execute(Runnable command) {
        schedule(Duration.ZERO, command);
    }

    @Override
    public Closeable schedule(Duration delay, Runnable command) {
        checkNotClosed();
        return clock.schedule(this, delay, command);
    }

    @Override
    public Closeable scheduleAtFixedRate(Duration initialDelay, Duration period, Runnable command) {
        checkNotClosed();
        return clock.scheduleAtFixedRate(this, initialDelay, period, command);
    }

    @Override
    public void doClose() {
        clock.closeExecutor(this);
        closed = true;
    }

    String getThreadNameBase() {
        return threadNameBase;
    }

    private void checkNotClosed() {
        checkState(!closed, "closed");
    }
}
