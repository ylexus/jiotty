package net.yudichev.jiotty.common.time;


import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TimeIntervalTest {
    @Test
    void isWhollyAfter() {
        var oneToTwoHours = interval("01:00:00", "02:00:00");
        assertThat(oneToTwoHours.isWhollyAfter(interval("00:00:00", "00:59:00"))).isTrue();
        assertThat(oneToTwoHours.isWhollyAfter(interval("00:00:00", "01:00:00"))).isTrue();
        assertThat(oneToTwoHours.isWhollyAfter(interval("00:00:00", "01:30:00"))).isFalse();
        assertThat(oneToTwoHours.isWhollyAfter(interval("01:00:00", "01:30:00"))).isFalse();
        assertThat(oneToTwoHours.isWhollyAfter(interval("01:30:00", "01:40:00"))).isFalse();
        assertThat(oneToTwoHours.isWhollyAfter(interval("01:30:00", "02:00:00"))).isFalse();
        assertThat(oneToTwoHours.isWhollyAfter(interval("01:30:00", "02:30:00"))).isFalse();
        assertThat(oneToTwoHours.isWhollyAfter(interval("02:00:00", "02:30:00"))).isFalse();
        assertThat(oneToTwoHours.isWhollyAfter(interval("02:30:00", "03:00:00"))).isFalse();
        assertThat(oneToTwoHours.isWhollyAfter(interval("00:00:00", "03:00:00"))).isFalse();
    }

    @Test
    void minus() {
        var oneToTwoHours = interval("01:00:00", "02:00:00");
        assertThat(oneToTwoHours.minus(interval("00:00:00", "00:59:00"))).containsExactly(oneToTwoHours);
        assertThat(oneToTwoHours.minus(interval("00:00:00", "01:00:00"))).containsExactly(oneToTwoHours);
        assertThat(oneToTwoHours.minus(interval("00:00:00", "01:30:00"))).containsExactly(interval("01:30:00", "02:00:00"));
        assertThat(oneToTwoHours.minus(interval("01:00:00", "01:30:00"))).containsExactly(interval("01:30:00", "02:00:00"));
        assertThat(oneToTwoHours.minus(interval("01:30:00", "01:40:00"))).containsExactly(interval("01:00:00", "01:30:00"), interval("01:40:00", "02:00:00"));
        assertThat(oneToTwoHours.minus(interval("01:30:00", "02:00:00"))).containsExactly(interval("01:00:00", "01:30:00"));
        assertThat(oneToTwoHours.minus(interval("01:30:00", "02:30:00"))).containsExactly(interval("01:00:00", "01:30:00"));
        assertThat(oneToTwoHours.minus(interval("02:00:00", "02:30:00"))).containsExactly(oneToTwoHours);
        assertThat(oneToTwoHours.minus(interval("02:30:00", "03:00:00"))).containsExactly(oneToTwoHours);
        assertThat(oneToTwoHours.minus(oneToTwoHours)).isEmpty();
        assertThat(oneToTwoHours.minus(interval("00:00:00", "03:00:00"))).isEmpty();
    }

    static TimeInterval interval(String start, String end) {
        return new TimeInterval(i(start), i(end));
    }

    static Instant i(String time) {
        return Instant.parse("2000-01-01T" + time + 'Z');
    }
}