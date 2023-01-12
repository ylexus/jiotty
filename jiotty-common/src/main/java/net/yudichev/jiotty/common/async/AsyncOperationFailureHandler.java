package net.yudichev.jiotty.common.async;

import java.util.Optional;

public interface AsyncOperationFailureHandler {
    /**
     * @return backoff delay applied, in milliseconds, or 0, if no backoff delay was applied
     */
    Optional<Long> handle(String operationName, Throwable exception);

    void reset();
}
