package net.yudichev.jiotty.common.async;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.assistedinject.Assisted;
import net.yudichev.jiotty.common.lang.BaseIdempotentCloseable;
import net.yudichev.jiotty.common.lang.Closeable;
import net.yudichev.jiotty.common.lang.Runnables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

final class SingleThreadedSchedulingExecutor implements SchedulingExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SingleThreadedSchedulingExecutor.class);

    private final Set<Closeable> scheduleHandles = Sets.newConcurrentHashSet();
    private final ScheduledExecutorService executor;

    @Inject
    SingleThreadedSchedulingExecutor(@Assisted String threadNameBase) {
        executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setNameFormat(threadNameBase + "-%s")
                .setDaemon(true)
                .build());
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(Runnables.guarded(logger, "task", command));
    }

    @Override
    public Closeable schedule(Duration delay, Runnable command) {
        Closeable scheduledHandle = new ScheduledHandle(executor.schedule(
                Runnables.guarded(logger, "scheduled task", command), delay.toNanos(), NANOSECONDS));
        scheduleHandles.add(scheduledHandle);
        return scheduledHandle;
    }

    @Override
    public Closeable scheduleAtFixedRate(Duration initialDelay, Duration period, Runnable command) {
        Closeable scheduledHandle = new ScheduledHandle(
                executor.scheduleAtFixedRate(Runnables.guarded(logger, "scheduled task", command), initialDelay.toNanos(), period.toNanos(), NANOSECONDS));
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

    private final class ScheduledHandle extends BaseIdempotentCloseable {
        private final Closeable executorHandle;

        private ScheduledHandle(Future<?> scheduledFuture) {
            executorHandle = () -> scheduledFuture.cancel(false);
        }

        @Override
        protected void doClose() {
            executorHandle.close();
            scheduleHandles.remove(this);
        }
    }
}
