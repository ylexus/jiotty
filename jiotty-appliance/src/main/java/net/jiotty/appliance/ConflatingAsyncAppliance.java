package net.jiotty.appliance;

import com.google.inject.BindingAnnotation;

import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.jiotty.appliance.Bindings.ApplianceExecutor;

final class ConflatingAsyncAppliance implements Appliance {
    private final AtomicReference<Command> queue = new AtomicReference<>();
    private final Appliance delegate;
    private final Executor executor;
    private CompletableFuture<?> lastResult;

    @Inject
    ConflatingAsyncAppliance(@ApplianceExecutor Executor executor,
                             @Delegate Appliance delegate) {
        this.executor = checkNotNull(executor);
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public Set<? extends Command> getAllSupportedCommands() {
        return delegate.getAllSupportedCommands();
    }

    @Override
    public String name() {
        return String.format("ConflatingAsync adaptor for %s @ %s", delegate.name(), System.identityHashCode(this));
    }

    @Override
    public CompletableFuture<?> execute(Command command) {
        return enqueueCommand(command);
    }

    private synchronized CompletableFuture<?> enqueueCommand(Command newCommand) {
        Command previousCommand = queue.getAndSet(newCommand);
        if (previousCommand == null) {
            lastResult = processQueue();
        }
        return lastResult;
    }

    private CompletableFuture<?> processQueue() {
        return CompletableFuture
                .supplyAsync(() -> delegate.execute(queue.getAndSet(null)), executor)
                .thenCompose(Function.identity());

    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Delegate {
    }
}
