package net.yudichev.jiotty.common.lang;

import java.util.concurrent.Future;

public final class Futures {
    private Futures() {
    }

    public static void cancel(Future<?> future) {
        if (future != null) {
            future.cancel(false);
        }
    }
}
