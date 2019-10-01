package net.jiotty.connector.world;

import net.jiotty.common.inject.LifecycleComponent;
import net.jiotty.common.lang.Closeable;

import java.util.concurrent.Executor;

/**
 * Courtesy of sunrise-sunset.org.
 */
public interface SunriseSunsetService extends LifecycleComponent {
    Closeable onEverySunrise(Runnable action, Executor executor);

    Closeable onEverySunset(Runnable action, Executor executor);
}
