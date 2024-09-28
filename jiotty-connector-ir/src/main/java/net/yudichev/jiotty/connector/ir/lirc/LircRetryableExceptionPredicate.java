package net.yudichev.jiotty.connector.ir.lirc;

import com.google.common.base.Predicate;

import java.net.SocketException;

final class LircRetryableExceptionPredicate implements Predicate<Throwable> {
    @Override
    public boolean apply(Throwable input) {
        return input instanceof SocketException
                // LIRC server failed to execute command; seen this happen: [1296690.602765] ir_toy 3-2:1.1: failed to send tx start command: -16
                || input instanceof LircServerException;
    }
}
