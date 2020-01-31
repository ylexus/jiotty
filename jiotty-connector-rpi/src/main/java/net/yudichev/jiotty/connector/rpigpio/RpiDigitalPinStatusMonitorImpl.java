package net.yudichev.jiotty.connector.rpigpio;

import com.google.inject.BindingAnnotation;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.Closeable;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.yudichev.jiotty.common.lang.CompositeException.runForAll;

final class RpiDigitalPinStatusMonitorImpl extends BaseLifecycleComponent implements RpiDigitalPinStatusMonitor {
    private final Provider<GpioController> gpioControllerProvider;
    private final Pin pin;
    private final PinPullResistance pinPullResistance;
    private final Set<Consumer<PinState>> listeners = new CopyOnWriteArraySet<>();

    private GpioPinDigitalInput input;
    private Closeable closeable;

    @Inject
    RpiDigitalPinStatusMonitorImpl(Provider<GpioController> gpioControllerProvider,
                                   @Dependency Pin pin,
                                   @Dependency PinPullResistance pinPullResistance) {
        this.gpioControllerProvider = checkNotNull(gpioControllerProvider);
        this.pin = checkNotNull(pin);
        this.pinPullResistance = checkNotNull(pinPullResistance);
    }

    @Override
    public Closeable addListener(Consumer<PinState> listener) {
        return whenStartedAndNotLifecycling(() -> {
            checkArgument(listeners.add(listener), "already added: %s", listener);
            listener.accept(input.getState());
            return Closeable.idempotent(() -> listeners.remove(listener));
        });
    }

    @Override
    protected void doStart() {
        input = gpioControllerProvider.get().provisionDigitalInputPin(pin, pinPullResistance);
        input.setShutdownOptions(true);
        GpioPinListenerDigital listener = event -> onListenerStateChange(event.getState());
        input.addListener(listener);
        closeable = Closeable.idempotent(() -> input.removeListener(listener));
    }

    @Override
    protected void doStop() {
        closeable.close();
    }

    private void onListenerStateChange(PinState state) {
        runForAll(listeners, pinStateConsumer -> pinStateConsumer.accept(state));
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }
}
