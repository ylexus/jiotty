package net.yudichev.jiotty.energy;

import net.yudichev.jiotty.common.async.JobSchedulerImpl;
import net.yudichev.jiotty.common.async.ProgrammableClock;
import net.yudichev.jiotty.common.lang.CompletableFutures;
import net.yudichev.jiotty.connector.octopusenergy.OctopusEnergy;
import net.yudichev.jiotty.connector.octopusenergy.StandardUnitRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("OctalInteger")
@ExtendWith(MockitoExtension.class)
class OctopusEnergyPriceServiceImplTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");
    private ProgrammableClock clock;
    @Mock
    private OctopusEnergy octopusEnergy;
    private OctopusEnergyPriceServiceImpl service;

    @BeforeEach
    void setUp() {
        clock = new ProgrammableClock().withMdc().withGlobalMdc(true);
        clock.setTime(time(1, 14, 0));
        var jobScheduler = new JobSchedulerImpl(clock, clock, ZoneOffset.UTC);
        var executor = clock.createSingleThreadedSchedulingExecutor("thread");
        service = new OctopusEnergyPriceServiceImpl(() -> executor, clock, octopusEnergy, jobScheduler, ZONE_ID);
        when(octopusEnergy.getAgilePrices(clock.currentInstant(), clock.currentInstant().plus(2, DAYS)))
                .thenReturn(completedFuture(prices(time(1, 14, 0), time(1, 23, 00))));

        jobScheduler.start();
        service.start();
        clock.tick();
        verifyNoMoreInteractions(octopusEnergy);
        assertPricesAvailable(time(1, 14, 00), time(1, 23, 00));
    }

    @Test
    void scenario() {
        for (int day = 1; day < 4; day += 2) {
            reset(octopusEnergy);

            // normal case - after 16:00 on day 1 prices are available until 23:00 next day
            when(octopusEnergy.getAgilePrices(time(day, 16, 05), time(day + 2, 16, 05)))
                    .thenReturn(completedFuture(prices(time(day, 16, 00), time(day + 1, 23, 00))));
            clock.setTimeAndTick(time(day, 16, 05).plusNanos(12));
            assertPricesAvailable(time(day, 16, 00), time(day + 1, 23, 00));
            verifyNoMoreInteractions(octopusEnergy);
            reset(octopusEnergy);

            // next day, for some reason, prices are not yet available for the day after, even after 16:00
            when(octopusEnergy.getAgilePrices(time(day + 1, 16, 05), time(day + 3, 16, 05)))
                    .thenReturn(completedFuture(prices(time(day + 1, 16, 00), time(day + 1, 23, 00))));
            clock.setTimeAndTick(time(day + 1, 16, 05));
            assertPricesAvailable(time(day + 1, 16, 00), time(day + 1, 23, 00));
            verifyNoMoreInteractions(octopusEnergy);

            // 1st retry - still not there
            clock.setTimeAndTick(time(day + 1, 16, 20));
            assertPricesAvailable(time(day + 1, 16, 00), time(day + 1, 23, 00));

            // 2nd retry - now we're talking
            reset(octopusEnergy);
            when(octopusEnergy.getAgilePrices(time(day + 1, 16, 05), time(day + 3, 16, 05)))
                    .thenReturn(completedFuture(prices(time(day + 1, 16, 00), time(day + 2, 23, 00))));
            clock.setTimeAndTick(time(day + 1, 16, 35));
            assertPricesAvailable(time(day + 1, 16, 00), time(day + 2, 23, 00));

            // ensure retries stopped
            reset(octopusEnergy);
            clock.setTimeAndTick(time(day + 1, 16, 50));
            verifyNoMoreInteractions(octopusEnergy);
        }
    }

    @Test
    void octopusFailure() {
        reset(octopusEnergy);
        when(octopusEnergy.getAgilePrices(any(), any())).thenReturn(CompletableFutures.failure("oops"));
        clock.setTimeAndTick(time(1, 16, 05));
        // then no new prices as octopus call failed
        assertThat(service.getPrices().get().profileEnd()).isEqualTo(time(1, 23, 00));

        // problems continues until day 2's regular call
        clock.setTimeAndTick(time(2, 16, 05));
        // then still no new prices
        assertThat(service.getPrices().get().profileEnd()).isEqualTo(time(1, 23, 00));

        // octopus recovered
        reset(octopusEnergy);
        when(octopusEnergy.getAgilePrices(eq(time(2, 16, 05)), any()))
                .thenReturn(completedFuture(prices(time(2, 16, 00), time(3, 23, 00))));
        clock.setTimeAndTick(time(2, 16, 20));
        assertPricesAvailable(time(2, 16, 00), time(3, 23, 00));
    }

    private void assertPricesAvailable(Instant from, Instant to) {
        var prices = service.getPrices();
        assertThat(prices.get().profileStart()).describedAs("profile start").isEqualTo(from);
        assertThat(prices.get().profileEnd()).describedAs("profile end").isEqualTo(to);
    }

    private static List<StandardUnitRate> prices(Instant from, Instant to) {
        var result = new ArrayList<StandardUnitRate>();
        Instant validTo = to;
        while (validTo.isAfter(from)) {
            result.add(StandardUnitRate.builder().setValidFrom(validTo.minus(30, MINUTES)).setValidTo(validTo).setValueExcVat(13).setValueIncVat(14).build());
            validTo = validTo.minus(30, MINUTES);
        }
        return result;
    }

    private static Instant time(int day, int hour, int min) {
        return LocalDateTime.of(2024, 1, day, hour, min, 00).atZone(ZONE_ID).toInstant();
    }
}