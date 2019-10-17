package net.yudichev.jiotty.common.testutil;

import net.yudichev.jiotty.common.async.ExecutorFactory;
import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.lang.Closeable;

import java.time.Duration;
import java.util.*;

import static java.util.Collections.unmodifiableList;

public final class DeterministicExecutorFactory implements ExecutorFactory {
    private final List<DeterministicExecutor> createdExecutors = new ArrayList<>();

    @Override
    public SchedulingExecutor createSingleThreadedSchedulingExecutor(String threadNameBase) {
        DeterministicExecutor executor = new DeterministicExecutor();
        createdExecutors.add(executor);
        return executor;
    }

    public List<DeterministicExecutor> getCreatedExecutors() {
        return unmodifiableList(createdExecutors);
    }

    public static class DeterministicExecutor implements SchedulingExecutor {
        private Collection<Runnable> commands = new ArrayList<>();
        private Map<Duration, List<Runnable>> scheduledTasksByDuration = new TreeMap<>();

        @Override
        public void execute(Runnable command) {
            commands.add(command);
        }

        @Override
        public Closeable schedule(Duration delay, Runnable command) {
            scheduledTasksByDuration.computeIfAbsent(delay, duration -> new ArrayList<>()).add(command);
            return Closeable.idempotent(() -> scheduledTasksByDuration.compute(delay, (duration, tasks) -> {
                if (tasks != null) {
                    tasks.remove(command);
                    if (tasks.isEmpty()) {
                        return null;
                    }
                }
                return tasks;
            }));
        }

        @Override
        public Closeable scheduleAtFixedRate(Duration initialDelay, Duration period, Runnable command) {
            throw new UnsupportedOperationException("scheduleAtFixedRate");
        }

        @Override
        public void close() {
        }

        public void executePendingCommands() {
            Collection<Runnable> currentCommands = commands;
            commands = new ArrayList<>();
            currentCommands.forEach(Runnable::run);
        }

        public void executePendingScheduledTasks() {
            Collection<List<Runnable>> currentCommands = scheduledTasksByDuration.values();
            scheduledTasksByDuration = new TreeMap<>();
            currentCommands.stream()
                    .flatMap(Collection::stream)
                    .forEach(Runnable::run);
        }
    }
}
