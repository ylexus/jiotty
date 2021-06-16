package net.yudichev.jiotty.common.lang.backoff;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SynchronizedBackOff implements BackOff {
    private final BackOff delegate;
    private final Object lock = new Object();

    public SynchronizedBackOff(BackOff delegate) {
        this.delegate = checkNotNull(delegate);
    }

    @Override
    public void reset() {
        synchronized (lock) {
            delegate.reset();
        }
    }

    @Override
    public long nextBackOffMillis() {
        synchronized (lock) {
            return delegate.nextBackOffMillis();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("delegate", delegate)
                .toString();
    }
}
