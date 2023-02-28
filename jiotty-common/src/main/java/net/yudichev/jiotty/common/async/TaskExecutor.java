package net.yudichev.jiotty.common.async;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface TaskExecutor extends Executor {
    <T> CompletableFuture<T> submit(Callable<T> task);

    default CompletableFuture<Void> submit(Runnable command) {
        return submit(toCallable(command));
    }

    @Override
    default void execute(Runnable command) {
        submit(toCallable(command));
    }

    static Callable<Void> toCallable(Runnable command) {
        return () -> {
            command.run();
            return null;
        };
    }
}
