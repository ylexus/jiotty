package net.yudichev.jiotty.connector.ir.lirc;

import com.google.common.base.Predicate;

import java.net.SocketException;

final class LircRetryableExceptionPredicate implements Predicate<Throwable> {
    @Override
    public boolean apply(Throwable input) {
        return input instanceof SocketException;
    }
}
