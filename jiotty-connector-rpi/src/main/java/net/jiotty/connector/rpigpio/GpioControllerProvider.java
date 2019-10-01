package net.jiotty.connector.rpigpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import net.jiotty.common.inject.BaseLifecycleComponent;

import javax.inject.Provider;

final class GpioControllerProvider extends BaseLifecycleComponent implements Provider<GpioController> {
    private GpioController gpio;

    @Override
    public GpioController get() {
        return gpio;
    }

    @Override
    public void doStart() {
        gpio = GpioFactory.getInstance();
    }

    @Override
    protected void doStop() {
        gpio.shutdown();
    }
}
