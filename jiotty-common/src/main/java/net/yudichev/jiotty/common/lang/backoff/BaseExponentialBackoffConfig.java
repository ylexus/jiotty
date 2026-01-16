package net.yudichev.jiotty.common.lang.backoff;

import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import static net.yudichev.jiotty.common.lang.backoff.ExponentialBackOff.DEFAULT_INITIAL_INTERVAL_MILLIS;
import static net.yudichev.jiotty.common.lang.backoff.ExponentialBackOff.DEFAULT_MAX_ELAPSED_TIME_MILLIS;
import static net.yudichev.jiotty.common.lang.backoff.ExponentialBackOff.DEFAULT_MAX_INTERVAL_MILLIS;
import static net.yudichev.jiotty.common.lang.backoff.ExponentialBackOff.DEFAULT_MULTIPLIER;
import static net.yudichev.jiotty.common.lang.backoff.ExponentialBackOff.DEFAULT_RANDOMIZATION_FACTOR;

@Immutable
@PublicImmutablesStyle
public interface BaseExponentialBackoffConfig {
    @Value.Default
    default long initialIntervalMillis() {
        return DEFAULT_INITIAL_INTERVAL_MILLIS;
    }

    @Value.Default
    default double randomizationFactor() {
        return DEFAULT_RANDOMIZATION_FACTOR;
    }

    @Value.Default
    default double multiplier() {
        return DEFAULT_MULTIPLIER;
    }

    @Value.Default
    default long maxIntervalMillis() {
        return DEFAULT_MAX_INTERVAL_MILLIS;
    }

    @Value.Default
    default long maxElapsedTimeMillis() {
        return DEFAULT_MAX_ELAPSED_TIME_MILLIS;
    }
}
