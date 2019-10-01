package net.jiotty.connector.rpigpio;

import com.pi4j.io.gpio.GpioController;
import net.jiotty.common.inject.BaseLifecycleComponentModule;
import net.jiotty.common.inject.ExposedKeyModule;

public final class GpioControllerModule extends BaseLifecycleComponentModule implements ExposedKeyModule<GpioController> {
    @Override
    protected void configure() {
        bind(getExposedKey()).toProvider(boundLifecycleComponent(GpioControllerProvider.class));
        expose(getExposedKey());
    }
}
