package net.jiotty.appliance;

import net.jiotty.common.lang.CompletableFutures;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.jiotty.appliance.ApplianceStatus.IN_TRANSITION;

public interface ApplianceStatusSensor {
    Duration DEFAULT_TIMEOUT = Duration.ofMinutes(2);

    CompletableFuture<Optional<ApplianceStatus>> awaitStatus(Predicate<ApplianceStatus> statusPredicate, Duration timeout);

    default CompletableFuture<Optional<ApplianceStatus>> awaitStatus(Predicate<ApplianceStatus> statusPredicate) {
        return awaitStatus(statusPredicate, DEFAULT_TIMEOUT);
    }

    default CompletableFuture<Optional<ApplianceStatus>> awaitAnyStatus() {
        return awaitStatus(ignored -> true);
    }

    default CompletableFuture<Optional<ApplianceStatus>> awaitStatus(ApplianceStatus targetStatus, Duration timeout) {
        return awaitStatus(status -> status == targetStatus, timeout);
    }

    default CompletableFuture<Optional<ApplianceStatus>> awaitStatus(ApplianceStatus targetStatus) {
        return awaitStatus(targetStatus, DEFAULT_TIMEOUT);
    }

    default CompletableFuture<Optional<ApplianceStatus>> awaitStableStatus() {
        return awaitStatus(status -> status != IN_TRANSITION);
    }

    static Function<Optional<ApplianceStatus>, CompletableFuture<ApplianceStatus>> failOnTimeout() {
        return applianceStatus -> applianceStatus
                .map(CompletableFuture::completedFuture)
                .orElse(CompletableFutures.failure("Took to long to wait for requested appliance status"));
    }
}
