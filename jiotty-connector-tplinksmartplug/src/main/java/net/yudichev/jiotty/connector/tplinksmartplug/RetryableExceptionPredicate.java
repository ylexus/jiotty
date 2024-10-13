package net.yudichev.jiotty.connector.tplinksmartplug;

import java.util.function.Predicate;

final class RetryableExceptionPredicate implements Predicate<Throwable> {
    @Override
    public boolean test(Throwable throwable) {
        return throwable.getMessage() == null || !throwable.getMessage().contains("API rate limit exceeded");
    }
}
