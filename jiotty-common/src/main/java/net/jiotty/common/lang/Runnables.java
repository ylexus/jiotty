package net.jiotty.common.lang;

import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Runnables {
    private Runnables() {
    }

    public static Runnable guarded(Logger logger, String taskDescription, Runnable delegate) {
        checkNotNull(logger);
        checkNotNull(delegate);
        return () -> {
            try {
                delegate.run();
            } catch (Throwable e) {
                logger.error("Failed while " + taskDescription, e);
            }
        };
    }
}
