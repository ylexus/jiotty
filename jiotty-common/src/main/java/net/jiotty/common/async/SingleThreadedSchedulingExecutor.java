package net.jiotty.common.async;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.assistedinject.Assisted;
import net.jiotty.common.lang.BaseIdempotentCloseable;
import net.jiotty.common.lang.Closeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static net.jiotty.common.lang.Runnables.guarded;

final class SingleThreadedSchedulingExecutor implements SchedulingExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SingleThreadedSchedulingExecutor.class);

    private final Set<Closeable> scheduleHandles = Sets.newConcurrentHashSet();
    private final ScheduledExecutorService executor;

    @Inject
    SingleThreadedSchedulingExecutor(@Assisted String threadNameBase) {
        executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat(threadNameBase + "-%s").build());
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(guarded(logger, "task", command));
    }

    @Override
    public Closeable schedule(Duration delay, Runnable command) {
        ScheduledHandle scheduledHandle = new ScheduledHandle(executor.schedule(
                guarded(logger, "scheduled task", command), delay.toNanos(), NANOSECONDS));
        scheduleHandles.add(scheduledHandle);
        return scheduledHandle;
    }

    @Override
    public Closeable scheduleAtFixedRate(Duration initialDelay, Duration period, Runnable command) {
        ScheduledHandle scheduledHandle = new ScheduledHandle(
                executor.scheduleAtFixedRate(guarded(logger, "scheduled task", command), initialDelay.toNanos(), period.toNanos(), NANOSECONDS));
        scheduleHandles.add(scheduledHandle);
        return scheduledHandle;
    }

    @Override
    public void close() {
        Closeable.forCloseables(scheduleHandles).close();
        if (!MoreExecutors.shutdownAndAwaitTermination(executor, 10, SECONDS)) {
            logger.warn("Was not able to gracefully stop executor in 10 seconds");
        }
    }

    private class ScheduledHandle extends BaseIdempotentCloseable {
        private Closeable executorHandle;

        private ScheduledHandle(ScheduledFuture<?> scheduledFuture) {
            this.executorHandle = () -> scheduledFuture.cancel(false);
        }

        @Override
        protected void doClose() {
            executorHandle.close();
            scheduleHandles.remove(this);
        }
    }
}
