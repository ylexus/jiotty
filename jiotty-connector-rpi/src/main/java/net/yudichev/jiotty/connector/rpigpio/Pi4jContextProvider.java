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

import static java.util.concurrent.TimeUnit.SECONDS;
import static net.yudichev.jiotty.common.lang.MoreThrowables.asUnchecked;

final class Pi4jContextProvider extends BaseLifecycleComponent implements Provider<Context> {
    private static final Logger logger = LoggerFactory.getLogger(Pi4jContextProvider.class);
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 15;

    private final CountDownLatch preShutdownLatch = new CountDownLatch(1);
    private final CountDownLatch postShutdownLatch = new CountDownLatch(1);
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
                    var stopTriggered = preShutdownLatch.await(SHUTDOWN_TIMEOUT_SECONDS, SECONDS);
                    if (stopTriggered) {
                        logger.debug("Stopping - pi4j shutdown released");
                    } else {
                        logger.warn("Timed out waiting for this component to be stopped");
                    }
                });
            }

            @Override
            public void onShutdown(ShutdownEvent event) {
                postShutdownLatch.countDown();
            }
        });
    }

    @Override
    protected void doStop() {
        logger.debug("Releasing pi4j shutdown gate and shutting down pi4j");
        preShutdownLatch.countDown();
        // best effort prevent double shutdown call and awaiting successful shutdown, will work if the pi4j shutdown hook fires first
        if (pi4jShuttingDown) {
            // pi4's own shutdown hook is doing the job - wait for shutdown to finish
            logger.debug("Waiting for pi4j to shut down");
            asUnchecked(() -> {
                var stopped = postShutdownLatch.await(SHUTDOWN_TIMEOUT_SECONDS, SECONDS);
                if (stopped) {
                    logger.debug("pi4j shut down");
                } else {
                    logger.warn("Timed out waiting for pi4 to shut down");
                }
            });
        } else {
            gpio.shutdown(); // does it synchronously
        }
    }
}
