package net.yudichev.jiotty.common.testutil;

import net.yudichev.jiotty.common.async.ExecutorFactory;
import net.yudichev.jiotty.common.async.SchedulingExecutor;

import java.util.ArrayList;
import java.util.List;

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
}
