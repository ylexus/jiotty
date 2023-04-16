package net.yudichev.jiotty.common.async;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.Closeable;
import net.yudichev.jiotty.common.lang.Runnables;
import net.yudichev.jiotty.common.time.CurrentDateTimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.lang.Closeable.closeIfNotNull;

final class JobSchedulerImpl extends BaseLifecycleComponent implements JobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(JobSchedulerImpl.class);

    private final ExecutorFactory executorFactory;
    private final CurrentDateTimeProvider currentDateTimeProvider;
    private SchedulingExecutor sharedScheduler;

    @Inject
    JobSchedulerImpl(ExecutorFactory executorFactory,
                     CurrentDateTimeProvider currentDateTimeProvider) {
        this.executorFactory = checkNotNull(executorFactory);
        this.currentDateTimeProvider = checkNotNull(currentDateTimeProvider);
    }

    @SuppressWarnings("ReturnOfInnerClass") // we are a singleton
    @Override
    public Closeable monthly(String jobName, int dayOfMonth, Runnable task) {
        return monthly(getSharedScheduler(), jobName, dayOfMonth, task);
    }

    @Override
    public Closeable monthly(Scheduler scheduler, String jobName, int dayOfMonth, Runnable task) {
        var job = new MonthlyJob(scheduler, jobName, dayOfMonth, task);
        job.scheduleNext();
        return job;
    }

    @Override
    public Closeable daily(String jobName, LocalTime time, Runnable task) {
        return daily(getSharedScheduler(), jobName, time, task);
    }

    @Override
    public Closeable daily(Scheduler scheduler, String jobName, LocalTime time, Runnable task) {
        var job = new DailyJob(scheduler, jobName, time, task);
        job.scheduleNext();
        return job;
    }

    private Scheduler getSharedScheduler() {
        return whenStartedAndNotLifecycling(() -> {
            if (sharedScheduler == null) {
                sharedScheduler = executorFactory.createSingleThreadedSchedulingExecutor("job-scheduler");
            }
            return sharedScheduler;
        });
    }

    @Override
    protected void doStop() {
        closeIfNotNull(sharedScheduler);
    }

    abstract class ScheduledJob implements Closeable {
        private final Scheduler scheduler;
        private final String jobName;
        private final Runnable task;
        private final LocalDateTime startTime;

        private int runNumber;
        private Closeable scheduleHandle;

        protected ScheduledJob(Scheduler scheduler, String jobName, Runnable task) {
            this.scheduler = checkNotNull(scheduler);
            this.jobName = checkNotNull(jobName);
            this.task = Runnables.guarded(logger, String.format("executing job %s", jobName), task);
            startTime = currentDateTimeProvider.currentDateTime();
        }

        @Override
        public void close() {
            scheduleHandle.close();
        }

        public final void scheduleNext() {
            var currentDateTime = currentDateTimeProvider.currentDateTime();
            LocalDateTime nextDateTime;
            while ((nextDateTime = calculateNextTime(startTime, runNumber++)).isBefore(currentDateTime)) {
                if (runNumber > 1) {
                    logger.warn("[{}] Previous job overran, next scheduled time should have been {} but now is {}, will skip this run",
                            jobName, nextDateTime, currentDateTime);
                }
            }

            LocalDateTime finalNextDateTime = nextDateTime;
            scheduleHandle = whenStartedAndNotLifecycling(() -> {
                Closeable handle = scheduler.schedule(Duration.between(currentDateTime, finalNextDateTime), this::trigger);
                logger.info("[{}] next job scheduled for {}", jobName, finalNextDateTime);
                return handle;
            });
        }

        protected abstract LocalDateTime calculateNextTime(LocalDateTime startTime, int runNumber);

        private void trigger() {
            logger.debug("[{}] executing", jobName);
            task.run();
            scheduleNext();
        }
    }

    private class MonthlyJob extends ScheduledJob {

        private final int dayOfMonth;

        MonthlyJob(Scheduler scheduler, String jobName, int dayOfMonth, Runnable task) {
            super(scheduler, jobName, task);
            this.dayOfMonth = dayOfMonth;
        }

        @Override
        protected LocalDateTime calculateNextTime(LocalDateTime startTime, int runNumber) {
            LocalDate dateNow = startTime.toLocalDate();
            return dateNow.plusMonths(runNumber).withDayOfMonth(dayOfMonth).atTime(3, 0, 0);
        }
    }

    private class DailyJob extends ScheduledJob {
        private final LocalTime time;

        DailyJob(Scheduler scheduler, String jobName, LocalTime time, Runnable task) {
            super(scheduler, jobName, task);
            this.time = checkNotNull(time);
        }

        @Override
        protected LocalDateTime calculateNextTime(LocalDateTime startTime, int runNumber) {
            return startTime.plusDays(runNumber).with(time);
        }
    }
}
