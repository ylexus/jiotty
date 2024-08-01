package net.yudichev.jiotty.connector.rpigpio;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.event.ShutdownEvent;
import com.pi4j.event.ShutdownListener;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MINUTES;
import static net.yudichev.jiotty.common.lang.MoreThrowables.asUnchecked;

final class Pi4jContextProvider extends BaseLifecycleComponent implements Provider<Context> {
    private static final Logger logger = LoggerFactory.getLogger(Pi4jContextProvider.class);

    private final CountDownLatch shutdownBlocker = new CountDownLatch(1);
    private volatile boolean pi4jShuttingDown;
    private Context gpio;

    @Override
    public Context get() {
        return whenStartedAndNotLifecycling(() -> gpio);
    }

    @Override
    public void doStart() {
        gpio = Pi4J.newAutoContext();
        // TODO this is a workaround for https://github.com/Pi4J/pi4j-v2/issues/371 - remove when fixed
        gpio.addListener(new ShutdownListener() {
            @Override
            public void beforeShutdown(ShutdownEvent event) {
                pi4jShuttingDown = true;
                logger.debug("Blocking pi4j shutdown until this component is closed");
                asUnchecked(() -> {
                    var stopTriggered = shutdownBlocker.await(1, MINUTES);
                    if (stopTriggered) {
                        logger.debug("Stopping - pi4j shutdown released");
                    } else {
                        logger.warn("Timed out waiting for this component to be stopped");
                    }
                });
            }

            @Override
            public void onShutdown(ShutdownEvent event) {
            }
        });
    }

    @Override
    protected void doStop() {
        logger.debug("Releasing pi4j shutdown gate and shutting down pi4j");
        shutdownBlocker.countDown();
        // best effort prevent double shutdown call, will work if the pi4j shutdown hook fires first
        if (!pi4jShuttingDown) {
            gpio.shutdown();
        }
    }
}
