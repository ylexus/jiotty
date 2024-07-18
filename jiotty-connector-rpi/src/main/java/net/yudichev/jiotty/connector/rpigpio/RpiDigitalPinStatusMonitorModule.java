package net.yudichev.jiotty.connector.rpigpio;

import com.pi4j.io.gpio.digital.PullResistance;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RpiDigitalPinStatusMonitorModule extends BaseLifecycleComponentModule implements ExposedKeyModule<RpiDigitalPinStatusMonitor> {
    private final Integer pin;
    private final PullResistance pullResistance;

    private RpiDigitalPinStatusMonitorModule(int pin, PullResistance pullResistance) {
        this.pin = pin;
        this.pullResistance = checkNotNull(pullResistance);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        bind(Integer.class).annotatedWith(RpiDigitalPinStatusMonitorImpl.Pin.class).toInstance(pin);
        bind(PullResistance.class).annotatedWith(RpiDigitalPinStatusMonitorImpl.Dependency.class).toInstance(pullResistance);
        bind(getExposedKey()).to(registerLifecycleComponent(RpiDigitalPinStatusMonitorImpl.class));
        expose(getExposedKey());
    }

    public static class Builder implements TypedBuilder<ExposedKeyModule<RpiDigitalPinStatusMonitor>> {
        private int pin;
        private PullResistance pullResistance;

        public Builder setPin(int pin) {
            this.pin = pin;
            return this;
        }

        public Builder setPullResistance(PullResistance pullResistance) {
            this.pullResistance = checkNotNull(pullResistance);
            return this;
        }

        @Override
        public ExposedKeyModule<RpiDigitalPinStatusMonitor> build() {
            return new RpiDigitalPinStatusMonitorModule(pin, pullResistance);
        }
    }
}
