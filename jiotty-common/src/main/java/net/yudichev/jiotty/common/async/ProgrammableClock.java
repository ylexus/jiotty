package net.yudichev.jiotty.common.async;

import net.yudichev.jiotty.common.lang.BaseIdempotentCloseable;
import net.yudichev.jiotty.common.lang.Closeable;
import net.yudichev.jiotty.common.time.CurrentDateTimeProvider;
import org.slf4j.MDC;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.*;

import static com.google.common.base.Preconditions.*;

public final class ProgrammableClock implements CurrentDateTimeProvider, ExecutorFactory {
    private final NavigableMap<Instant, List<Task>> tasksByTriggerTime = new TreeMap<>();
    private Instant currentTime = Instant.EPOCH;
    private boolean mdc;
    @Nullable
    private Instant currentTaskTime;

    public ProgrammableClock withMdc() {
        mdc = true;
        return this;
    }

    public void tick() {
        executeDueTasks();
    }

    public void setTimeAndTick(Instant time) {
        setCurrentTime(time);
        tick();
    }

    public void advanceTimeAndTick(TemporalAmount increment) {
        advanceTime(increment);
        tick();
    }

    @Override
    public LocalDateTime currentDateTime() {
        return currentInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public Instant currentInstant() {
        return currentTaskTime == null ? currentTime : currentTaskTime;
    }

    @Override
    public SchedulingExecutor createSingleThreadedSchedulingExecutor(String threadNameBase) {
        return new DeterministicExecutor(this, threadNameBase);
    }

    public void executeDueTasks() {
        Instant now = currentInstant();
        boolean pendingTasks = !tasksByTriggerTime.isEmpty();
        while (pendingTasks) {
            Optional<List<Task>> tasksDueAtEarliestInstant = tasksByTriggerTime.headMap(now, true).values().stream().findFirst();
            tasksDueAtEarliestInstant.ifPresent(tasks -> new ArrayList<>(tasks).forEach(Task::run));
            pendingTasks = tasksDueAtEarliestInstant.isPresent();
        }
    }

    Closeable schedule(DeterministicExecutor executor, TemporalAmount delay, Runnable command) {
        Task task = new Task(executor) {
            @Override
            public void doRun() {
                command.run();
                unSchedule(); // one-time task
            }
        };
        task.schedule(currentInstant().plus(delay));
        return task;

    }

    Closeable scheduleAtFixedRate(DeterministicExecutor executor, TemporalAmount initialDelay, TemporalAmount period, Runnable command) {
        Task task = new Task(executor) {
            @Override
            public void doRun() {
                command.run();
                Instant wasDue = unSchedule();
                schedule(wasDue.plus(period));
            }
        };
        task.schedule(currentInstant().plus(initialDelay));
        return task;
    }

    void closeExecutor(DeterministicExecutor executor) {
        executeDueTasks();
        removeTasksOf(executor);
    }

    private void removeTasksOf(DeterministicExecutor executor) {
        Iterator<List<Task>> taskIterator = tasksByTriggerTime.values().iterator();
        while (taskIterator.hasNext()) {
            List<Task> tasks = taskIterator.next();
            tasks.removeIf(task -> task.executor == executor);
            if (tasks.isEmpty()) {
                taskIterator.remove();
            }
        }
    }

    private void setCurrentTime(Instant currentTime) {
        checkArgument(currentTime.isAfter(this.currentTime) || currentTime.equals(this.currentTime),
                "time cannot go backward from %s to %s", this.currentTime, currentTime);
        this.currentTime = checkNotNull(currentTime);
    }

    private void advanceTime(TemporalAmount increment) {
        setCurrentTime(currentTime.plus(increment));
    }

    private abstract class Task extends BaseIdempotentCloseable implements Runnable {
        private final DeterministicExecutor executor;
        @Nullable
        protected Instant due;

        protected Task(DeterministicExecutor executor) {
            this.executor = checkNotNull(executor);
        }

        @Override
        public final void run() {
            checkState(due != null, "cannot run task %s - not scheduled", this);
            if (mdc) {
                MDC.put("thread", executor.getThreadNameBase());
                MDC.put("task.due", due.toString());
                MDC.put("current.time", currentInstant().toString());
            }
            currentTaskTime = due;
            try {
                doRun();
            } finally {
                currentTaskTime = null;
                if (mdc) {
                    MDC.remove("current.time");
                    MDC.remove("task.due");
                    MDC.remove("thread");
                }
            }
        }

        public void schedule(Instant due) {
            checkState(this.due == null, "already scheduled");
            this.due = due;
            tasksByTriggerTime.computeIfAbsent(due, instant -> new ArrayList<>()).add(this);
        }

        public final Instant unSchedule() {
            checkState(due != null, "not scheduled");
            tasksByTriggerTime.compute(due, (duration, tasks) -> {
                checkState(tasks != null, "unexpected: task {} not found", this);
                tasks.remove(this);
                if (tasks.isEmpty()) {
                    return null;
                }
                return tasks;
            });
            Instant wasDue = due;
            due = null;
            return wasDue;
        }

        protected abstract void doRun();

        @Override
        protected final void doClose() {
            if (due != null) {
                unSchedule();
            }
        }
    }
}
