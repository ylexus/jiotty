package net.yudichev.jiotty.common.async.backoff;

import com.google.inject.BindingAnnotation;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import net.yudichev.jiotty.common.lang.backoff.BackOff;
import net.yudichev.jiotty.common.lang.backoff.ExponentialBackOff;
import net.yudichev.jiotty.common.lang.backoff.SynchronizedBackOff;
import org.immutables.value.Value;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.toIntExact;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

final class BackOffProvider implements Provider<BackOff> {
    private final BackOffConfig config;

    @Inject
    BackOffProvider(@Dependency BackOffConfig config) {
        this.config = checkNotNull(config);
    }

    @Override
    public BackOff get() {
        return new SynchronizedBackOff(new ExponentialBackOff.Builder()
                                               .setInitialIntervalMillis(toIntExact(config.initialInterval().toMillis()))
                                               .setMaxIntervalMillis(toIntExact(config.maxInterval().toMillis()))
                                               .setMultiplier(config.multiplier())
                                               .setRandomizationFactor(config.randomizationFactor())
                                               .setMaxElapsedTimeMillis(toIntExact(config.maxElapsedTime().toMillis()))
                                               .build());
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }

    @Value.Immutable
    @PublicImmutablesStyle
    interface BaseBackOffConfig {
        @Value.Default
        default Duration maxInterval() {
            return Duration.ofMillis(ExponentialBackOff.DEFAULT_MAX_INTERVAL_MILLIS);
        }

        @Value.Default
        default Duration initialInterval() {
            return Duration.ofMillis(ExponentialBackOff.DEFAULT_INITIAL_INTERVAL_MILLIS);
        }

        @Value.Default
        default Duration maxElapsedTime() {
            return Duration.ofMillis(ExponentialBackOff.DEFAULT_MAX_ELAPSED_TIME_MILLIS);
        }

        @Value.Default
        default double multiplier() {
            return ExponentialBackOff.DEFAULT_MULTIPLIER;
        }

        @Value.Default
        default double randomizationFactor() {
            return ExponentialBackOff.DEFAULT_RANDOMIZATION_FACTOR;
        }
    }
}
