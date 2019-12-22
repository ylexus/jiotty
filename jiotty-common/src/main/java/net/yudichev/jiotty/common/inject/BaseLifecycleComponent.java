package net.yudichev.jiotty.common.inject;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkState;

@SuppressWarnings("AbstractClassWithoutAbstractMethods") // designed for extension
public abstract class BaseLifecycleComponent implements LifecycleComponent {
    private final AtomicBoolean started = new AtomicBoolean();

    @Override
    public final void start() {
        checkState(!started.get(), "Component %s is already started", this);
        doStart();
        started.set(true);
    }

    @Override
    public final void stop() {
        if (started.getAndSet(false)) {
            doStop();
        }
    }

    protected final boolean isStarted() {
        return started.get();
    }

    protected final void checkStarted() {
        checkState(isStarted(), "Component %s is not started or already stopped", this);
    }

    protected void doStart() {
    }

    protected void doStop() {
    }
}
