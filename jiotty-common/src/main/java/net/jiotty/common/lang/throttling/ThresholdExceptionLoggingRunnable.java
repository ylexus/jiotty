package net.jiotty.common.lang.throttling;

import org.slf4j.Logger;

import javax.annotation.concurrent.NotThreadSafe;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.jiotty.common.lang.throttling.ThresholdGatedConsumer.thresholdGated;

@NotThreadSafe
public final class ThresholdExceptionLoggingRunnable implements Runnable {
    private final Runnable delegate;
    private final ThresholdGatedConsumer<RuntimeException> errorLoggingAction;

    private ThresholdExceptionLoggingRunnable(Runnable delegate, ThresholdGatedConsumer<RuntimeException> exceptionLoggingAction) {
        this.delegate = checkNotNull(delegate);
        this.errorLoggingAction = checkNotNull(exceptionLoggingAction);
    }

    public static Runnable withExceptionLoggedAfterThreshold(Logger logger, String description, int threshold, Runnable delegate) {
        return new ThresholdExceptionLoggingRunnable(delegate, thresholdGated(threshold, e -> logger.error("Error when " + description, e)));
    }

    @Override
    public void run() {
        try {
            delegate.run();
            errorLoggingAction.reset();
        } catch (RuntimeException e) {
            errorLoggingAction.accept(e);
        }
    }
}
