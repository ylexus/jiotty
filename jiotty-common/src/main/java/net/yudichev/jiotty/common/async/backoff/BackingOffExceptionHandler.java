package net.yudichev.jiotty.common.async.backoff;

import java.util.Optional;

interface BackingOffExceptionHandler {
    /// @return backoff delay applied, in milliseconds, or 0, if no backoff delay was applied
    Optional<Long> handle(String operationName, Throwable exception);

    void reset();
}
