package net.yudichev.jiotty.connector.rpigpio;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RpiDigitalPinStatusMonitorModule extends BaseLifecycleComponentModule implements ExposedKeyModule<RpiDigitalPinStatusMonitor> {
    private final Pin pin;
    private final PinPullResistance pinPullResistance;

    private RpiDigitalPinStatusMonitorModule(Pin pin, PinPullResistance pinPullResistance) {
        this.pin = checkNotNull(pin);
        this.pinPullResistance = checkNotNull(pinPullResistance);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        bind(Pin.class).annotatedWith(RpiDigitalPinStatusMonitorImpl.Dependency.class).toInstance(pin);
        bind(PinPullResistance.class).annotatedWith(RpiDigitalPinStatusMonitorImpl.Dependency.class).toInstance(pinPullResistance);
        bind(getExposedKey()).to(boundLifecycleComponent(RpiDigitalPinStatusMonitorImpl.class));
        expose(getExposedKey());
    }

    public static class Builder implements TypedBuilder<ExposedKeyModule<RpiDigitalPinStatusMonitor>> {
        private Pin pin;
        private PinPullResistance pinPullResistance;

        public Builder setPin(Pin pin) {
            this.pin = checkNotNull(pin);
            return this;
        }

        public Builder setPinPullResistance(PinPullResistance pinPullResistance) {
            this.pinPullResistance = checkNotNull(pinPullResistance);
            return this;
        }

        @Override
        public ExposedKeyModule<RpiDigitalPinStatusMonitor> build() {
            return new RpiDigitalPinStatusMonitorModule(pin, pinPullResistance);
        }
    }
}
