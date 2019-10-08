package net.yudichev.jiotty.connector.world;

import net.yudichev.jiotty.common.inject.LifecycleComponent;
import net.yudichev.jiotty.common.lang.Closeable;

import java.util.concurrent.Executor;

/**
 * Courtesy of sunrise-sunset.org.
 */
public interface SunriseSunsetService extends LifecycleComponent {
    Closeable onEverySunrise(Runnable action, Executor executor);

    Closeable onEverySunset(Runnable action, Executor executor);
}
