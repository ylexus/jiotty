package net.yudichev.jiotty.common.testutil;

import net.yudichev.jiotty.common.async.SchedulingExecutor;
import net.yudichev.jiotty.common.lang.Closeable;
import net.yudichev.jiotty.common.lang.PackagePrivateImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.time.Duration;
import java.util.*;

public final class DeterministicExecutor implements SchedulingExecutor {
    private Collection<Runnable> commands = new ArrayList<>();
    private Map<Duration, List<Runnable>> scheduledTasksByDuration = new TreeMap<>();
    private final Map<Duration, List<ScheduledPeriodicTask>> scheduledPeriodicTasksByInitialDelay = new TreeMap<>();

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
        ScheduledPeriodicTask scheduledPeriodicTask = ScheduledPeriodicTask.of(period, command);
        scheduledPeriodicTasksByInitialDelay
                .computeIfAbsent(initialDelay, duration -> new ArrayList<>())
                .add(scheduledPeriodicTask);
        return Closeable.idempotent(() -> scheduledPeriodicTasksByInitialDelay.compute(initialDelay, (duration, tasks) -> {
            if (tasks != null) {
                tasks.remove(scheduledPeriodicTask);
                if (tasks.isEmpty()) {
                    return null;
                }
            }
            return tasks;
        }));
    }

    @Override
    public void close() {
    }

    public void executePendingCommandsUntilIdle() {
        while (!commands.isEmpty()) {
            executePendingCommands();
        }
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

    public void executePeriodicTasks() {
        scheduledPeriodicTasksByInitialDelay.values().stream()
                .flatMap(Collection::stream)
                .forEach(scheduledPeriodicTask -> scheduledPeriodicTask.task().run());
    }

    @Immutable
    @PackagePrivateImmutablesStyle
    interface BaseScheduledPeriodicTask {
        @Value.Parameter
        Duration period();

        @Value.Parameter
        Runnable task();
    }
}
