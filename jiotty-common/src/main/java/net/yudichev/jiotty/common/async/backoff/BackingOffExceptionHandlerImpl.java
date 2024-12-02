package net.yudichev.jiotty.common.async.backoff;

import com.google.common.base.MoreObjects;
import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.common.lang.backoff.BackOff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Throwables.getCausalChain;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.yudichev.jiotty.common.lang.HumanReadableExceptionMessage.humanReadableMessage;
import static net.yudichev.jiotty.common.lang.MoreThrowables.asUnchecked;

final class BackingOffExceptionHandlerImpl implements BackingOffExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(BackingOffExceptionHandlerImpl.class);
    private final BackOff backOff;
    private final Predicate<? super Throwable> retryableExceptionPredicate;

    @Inject
    BackingOffExceptionHandlerImpl(@Dependency BackOff backOff, @Dependency Predicate<? super Throwable> retryableExceptionPredicate) {
        this.backOff = checkNotNull(backOff);
        this.retryableExceptionPredicate = checkNotNull(retryableExceptionPredicate);
    }

    @Override
    public Optional<Long> handle(String operationName, Throwable exception) {
        return getCausalChain(exception).stream()
                                        .filter(retryableExceptionPredicate)
                                        .findFirst()
                                        .map(throwable -> {
                                            long backOffMs = backOff.nextBackOffMillis();
                                            logger.debug("Operation '{}': backoff: {}", operationName, backOff);
                                            checkState(backOffMs != BackOff.STOP,
                                                       "Operation %s is being retried for too long (%sms) - giving up, last error was: %s",
                                                       operationName, backOff.getMaxElapsedTimeMillis(), humanReadableMessage(throwable));
                                            logger.debug("Retryable exception performing operation '{}', backing off by waiting for {}ms",
                                                         operationName,
                                                         backOffMs,
                                                         throwable);
                                            asUnchecked(() -> Thread.sleep(backOffMs));
                                            return Optional.of(backOffMs);
                                        })
                                        .orElse(Optional.empty());
    }

    @Override
    public void reset() {
        asUnchecked(backOff::reset);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("backOff", backOff)
                          .toString();
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }
}
