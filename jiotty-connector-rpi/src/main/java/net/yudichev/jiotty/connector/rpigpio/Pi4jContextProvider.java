package net.yudichev.jiotty.connector.rpigpio;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import jakarta.inject.Provider;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;

final class Pi4jContextProvider extends BaseLifecycleComponent implements Provider<Context> {
    private Context gpio;

    @Override
    public Context get() {
        return whenStartedAndNotLifecycling(() -> gpio);
    }

    @Override
    public void doStart() {
        gpio = Pi4J.newAutoContext();
    }

    @Override
    protected void doStop() {
        gpio.shutdown(); // does it synchronously
    }
}
