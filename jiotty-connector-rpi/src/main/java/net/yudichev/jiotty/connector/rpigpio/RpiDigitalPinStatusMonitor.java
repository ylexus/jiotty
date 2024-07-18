package net.yudichev.jiotty.connector.rpigpio;

import com.pi4j.io.gpio.digital.DigitalState;
import net.yudichev.jiotty.common.lang.Closeable;

import java.util.function.Consumer;

public interface RpiDigitalPinStatusMonitor {
    Closeable addListener(Consumer<DigitalState> listener);
}
