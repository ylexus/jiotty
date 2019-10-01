package net.jiotty.common.lang;

import javax.annotation.Nonnull;
import java.util.concurrent.*;

final class DelayedExecutors {
    private DelayedExecutors() {
    }

    static Executor delayedExecutor(long millis) {
        return new DelayedExecutor(millis, TimeUnit.MILLISECONDS, ForkJoinPool.commonPool());
    }

    /**
     * Singleton delay scheduler, used only for starting and
     * cancelling tasks.
     */
    static final class Delayer {
        static final ScheduledThreadPoolExecutor delayer;

        static {
            (delayer = new ScheduledThreadPoolExecutor(
                    1, new DaemonThreadFactory())).
                    setRemoveOnCancelPolicy(true);
        }

        static void delay(Runnable command, long delay, TimeUnit unit) {
            delayer.schedule(command, delay, unit);
        }

        static final class DaemonThreadFactory implements ThreadFactory {
            @Override
            public Thread newThread(@Nonnull Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("CompletableFutureDelayScheduler");
                return t;
            }
        }
    }

    // Little class-ified lambdas to better support monitoring

    static final class DelayedExecutor implements Executor {
        final long delay;
        final TimeUnit unit;
        final Executor executor;

        DelayedExecutor(long delay, TimeUnit unit, Executor executor) {
            this.delay = delay;
            this.unit = unit;
            this.executor = executor;
        }

        @Override
        public void execute(@Nonnull Runnable r) {
            Delayer.delay(new TaskSubmitter(executor, r), delay, unit);
        }
    }

    /**
     * Action to submit user task
     */
    static final class TaskSubmitter implements Runnable {
        final Executor executor;
        final Runnable action;

        TaskSubmitter(Executor executor, Runnable action) {
            this.executor = executor;
            this.action = action;
        }

        @Override
        public void run() {
            executor.execute(action);
        }
    }
}
