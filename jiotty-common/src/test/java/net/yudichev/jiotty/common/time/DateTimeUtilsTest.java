package net.yudichev.jiotty.common.time;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateTimeUtilsTest {

    @Test
    void getTimeIntervalsMatchingStartEndTime_overnightExampleFromIssue() {
        ZoneId zone = ZoneOffset.UTC;
        ZonedDateTime from = zdt("2025-01-01T01:45:00Z");
        ZonedDateTime to = zdt("2025-01-02T01:45:00Z");
        LocalTime start = LocalTime.of(23, 30);
        LocalTime end = LocalTime.of(8, 0);

        List<TimeInterval> intervals = DateTimeUtils.getTimeIntervalsMatchingStartEndTime(from, to, start, end);

        assertThat(intervals).containsExactly(
                ti(zdt("2025-01-01T01:45:00Z").toInstant(), zdt("2025-01-01T08:00:00Z").toInstant()),
                ti(zdt("2025-01-01T23:30:00Z").toInstant(), zdt("2025-01-02T01:45:00Z").toInstant())
        );
        // sanity: zones
        assertThat(from.getZone()).isEqualTo(zone);
        assertThat(to.getZone()).isEqualTo(zone);
    }

    @Test
    void getTimeIntervalsMatchingStartEndTime_sameDayWindowAcrossMultipleDays() {
        ZonedDateTime from = zdt("2025-01-01T06:00:00Z");
        ZonedDateTime to = zdt("2025-01-03T10:00:00Z");
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(12, 0);

        List<TimeInterval> intervals = DateTimeUtils.getTimeIntervalsMatchingStartEndTime(from, to, start, end);

        assertThat(intervals).containsExactly(
                ti(zdt("2025-01-01T08:00:00Z").toInstant(), zdt("2025-01-01T12:00:00Z").toInstant()),
                ti(zdt("2025-01-02T08:00:00Z").toInstant(), zdt("2025-01-02T12:00:00Z").toInstant()),
                ti(zdt("2025-01-03T08:00:00Z").toInstant(), zdt("2025-01-03T10:00:00Z").toInstant())
        );
    }

    @Test
    void getTimeIntervalsMatchingStartEndTime_boundariesAlignedOvernightSingleInterval() {
        ZonedDateTime from = zdt("2025-01-01T23:30:00Z");
        ZonedDateTime to = zdt("2025-01-02T08:00:00Z");
        List<TimeInterval> intervals =
                DateTimeUtils.getTimeIntervalsMatchingStartEndTime(from, to, LocalTime.of(23, 30), LocalTime.of(8, 0));
        assertThat(intervals).containsExactly(
                ti(zdt("2025-01-01T23:30:00Z").toInstant(), zdt("2025-01-02T08:00:00Z").toInstant())
        );
    }

    @Test
    void getTimeIntervalsMatchingStartEndTime_startEqualsEndProducesEmpty() {
        ZonedDateTime from = zdt("2025-01-01T00:00:00Z");
        ZonedDateTime to = zdt("2025-01-02T00:00:00Z");
        assertThat(DateTimeUtils.getTimeIntervalsMatchingStartEndTime(from, to, LocalTime.of(8, 0), LocalTime.of(8, 0)))
                .isEmpty();
    }

    @Test
    void getTimeIntervalsMatchingStartEndTime_emptyRangeProducesEmpty() {
        ZonedDateTime t = zdt("2025-01-01T00:00:00Z");
        assertThat(DateTimeUtils.getTimeIntervalsMatchingStartEndTime(t, t, LocalTime.of(0, 0), LocalTime.of(23, 59)))
                .isEmpty();
    }

    @Test
    void getTimeIntervalsMatchingStartEndTime_zoneMismatchThrows() {
        ZonedDateTime from = zdt("2025-01-01T00:00:00Z");
        ZonedDateTime to = from.withZoneSameInstant(ZoneId.of("Europe/London"));
        assertThatThrownBy(() -> DateTimeUtils.getTimeIntervalsMatchingStartEndTime(from,
                                                                                                                    to,
                                                                                                                    LocalTime.of(1, 0),
                                                                                                                    LocalTime.of(2, 0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getTimeIntervalsMatchingStartEndTime_dstSpringForwardEuropeLondonHandled() {
        ZoneId zone = ZoneId.of("Europe/London");
        // DST starts on 2025-03-30 in UK. Clock jumps from 01:00 UTC to 02:00 local -> 01:00 local becomes 02:00.
        // Use a window that spans the gap: 00:30-03:30 local
        ZonedDateTime from = ZonedDateTime.of(2025, 3, 30, 0, 0, 0, 0, zone);
        ZonedDateTime to = ZonedDateTime.of(2025, 3, 31, 0, 0, 0, 0, zone);
        LocalTime start = LocalTime.of(0, 30);
        LocalTime end = LocalTime.of(3, 30);

        List<TimeInterval> intervals = DateTimeUtils.getTimeIntervalsMatchingStartEndTime(from, to, start, end);
        assertThat(intervals).containsExactly(ti(zdtOffset("2025-03-30T00:30:00+00:00"), zdtOffset("2025-03-30T03:30:00+01:00")));
    }

    private static TimeInterval ti(Instant start, Instant end) {
        return new TimeInterval(start, end);
    }

    private static ZonedDateTime zdt(String isoInstant) {
        return Instant.parse(isoInstant).atZone(ZoneOffset.UTC);
    }

    private static Instant zdtOffset(String isoInstant) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(isoInstant, ZonedDateTime::from).toInstant();
    }
}
