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

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.lang.Closeable.closeIfNotNull;

final class JobSchedulerImpl extends BaseLifecycleComponent implements JobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(JobSchedulerImpl.class);

    private final ExecutorFactory executorFactory;
    private final CurrentDateTimeProvider currentDateTimeProvider;
    private SchedulingExecutor schedulingExecutor;

    @Inject
    JobSchedulerImpl(ExecutorFactory executorFactory,
                     CurrentDateTimeProvider currentDateTimeProvider) {
        this.executorFactory = checkNotNull(executorFactory);
        this.currentDateTimeProvider = checkNotNull(currentDateTimeProvider);
    }

    @SuppressWarnings("ReturnOfInnerClass") // we are a singleton
    @Override
    public Closeable monthly(String jobName, int dayOfMonth, Runnable task) {
        return new MonthlyJob(jobName, dayOfMonth, task);
    }

    @Override
    protected void doStart() {
        schedulingExecutor = executorFactory.createSingleThreadedSchedulingExecutor("job-scheduler");
    }

    @Override
    protected void doStop() {
        closeIfNotNull(schedulingExecutor);
    }

    private class MonthlyJob implements Closeable {
        private final String jobName;
        private final int dayOfMonth;
        private final Runnable task;
        private Closeable scheduleHandle;

        MonthlyJob(String jobName, int dayOfMonth, Runnable task) {
            this.jobName = checkNotNull(jobName);
            this.dayOfMonth = dayOfMonth;
            this.task = Runnables.guarded(logger, String.format("executing job %s for day of month %s", jobName, dayOfMonth), task);
            scheduleNext();
        }

        @Override
        public void close() {
            scheduleHandle.close();
        }

        private void scheduleNext() {
            LocalDateTime dateTimeNow = currentDateTimeProvider.currentDateTime();
            LocalDate dateNow = dateTimeNow.toLocalDate();
            LocalDateTime nextDateTime = dateNow.plusMonths(1).withDayOfMonth(dayOfMonth).atTime(3, 0, 0);

            scheduleHandle = whenStartedAndNotLifecycling(() -> {
                Closeable handle = schedulingExecutor.schedule(Duration.between(dateTimeNow, nextDateTime), this::trigger);
                logger.info("Next [{}] job scheduled for {}", jobName, nextDateTime);
                return handle;
            });
        }

        private void trigger() {
            scheduleNext();
            task.run();
        }
    }
}
