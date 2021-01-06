package net.yudichev.jiotty.appliance;

import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.common.async.backoff.RetryableOperationExecutor;

import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public final class RetryingAppliance implements Appliance {
    private final Appliance delegate;
    private final RetryableOperationExecutor retryableOperationExecutor;

    @Inject
    public RetryingAppliance(@Dependency Appliance delegate,
                             @Dependency RetryableOperationExecutor retryableOperationExecutor) {
        this.delegate = checkNotNull(delegate);
        this.retryableOperationExecutor = checkNotNull(retryableOperationExecutor);
    }

    @Override
    public CompletableFuture<?> execute(Command command) {
        return retryableOperationExecutor.withBackOffAndRetry(
                "execute " + command + " on " + delegate,
                () -> delegate.execute(command));
    }

    @Override
    public Set<? extends Command> getAllSupportedCommands() {
        return delegate.getAllSupportedCommands();
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }
}
