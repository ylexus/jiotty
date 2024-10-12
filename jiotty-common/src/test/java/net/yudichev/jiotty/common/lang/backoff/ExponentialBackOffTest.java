package net.yudichev.jiotty.common.lang.backoff;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

class ExponentialBackOffTest {
    @Test
    void fromOneMs() {
        var backOff = new ExponentialBackOff.Builder()
                .setInitialIntervalMillis(1)
                .setMultiplier(1.5)
                .setMaxIntervalMillis(100)
                .setRandomizationFactor(0.05)
                .setMaxElapsedTimeMillis(Integer.MAX_VALUE)
                .build();
        for (int i = 0; i < 13; i++) {
            backOff.nextBackOffMillis();
        }
        assertThat((double) backOff.nextBackOffMillis(), Matchers.closeTo(100, 5));
    }
}