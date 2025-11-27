package net.yudichev.jiotty.common.lang;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

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
            delayer = new ScheduledThreadPoolExecutor(1, new DaemonThreadFactory());
            delayer.setRemoveOnCancelPolicy(true);
        }

        private Delayer() {
        }

        static void delay(Runnable command, long delay, TimeUnit unit) {
            delayer.schedule(command, delay, unit);
        }

        static final class DaemonThreadFactory implements ThreadFactory {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("CompletableFutureDelayScheduler");
                return t;
            }
        }
    }

    private record DelayedExecutor(long delay, TimeUnit unit, Executor executor) implements Executor {

        @Override
            public void execute(Runnable command) {
                Delayer.delay(new TaskSubmitter(executor, command), delay, unit);
            }
        }

    /**
         * Action to submit user task
         */
        private record TaskSubmitter(Executor executor, Runnable action) implements Runnable {

        @Override
            public void run() {
                executor.execute(action);
            }
        }
}
