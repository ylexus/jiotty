package net.yudichev.jiotty.common.app;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.*;
import net.yudichev.jiotty.common.inject.LifecycleComponent;
import net.yudichev.jiotty.common.lang.MoreThrowables;
import net.yudichev.jiotty.common.lang.TypedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

public final class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
    }

    private final List<LifecycleComponent> componentsAttemptedToStart = new CopyOnWriteArrayList<>();
    private final AtomicBoolean restarting = new AtomicBoolean();
    private final AtomicBoolean jvmShuttingDown = new AtomicBoolean();
    private final AtomicBoolean startedAllComponentsSuccessfully = new AtomicBoolean();
    private final AtomicBoolean runCalled = new AtomicBoolean();
    private final Injector injector;

    private CountDownLatch shutdownLatch;
    private CountDownLatch fullyStoppedLatch;
    private Thread runThread;

    private Application(Supplier<Module> moduleSupplier) {
        ApplicationLifecycleControl applicationLifecycleControl = new ApplicationLifecycleControl() {
            @Override
            public void initiateShutdown() {
                logger.info("Application requested shutdown");
                initiateStop();
            }

            @Override
            public void initiateRestart() {
                checkState(!jvmShuttingDown.get(), "Cannot initiate restart while JVM is shutting down");
                checkState(restarting.compareAndSet(false, true), "Already restarting");
                logger.info("Application requested restart");
                initiateStop();
            }
        };

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            jvmShuttingDown.set(true);
            if (fullyStoppedLatch.getCount() > 0) {
                logger.info("Shutdown hook fired");
                initiateStop();
                MoreThrowables.asUnchecked(() -> {
                    if (!fullyStoppedLatch.await(1, TimeUnit.MINUTES)) {
                        logger.warn("Timed out waiting for partially initialised application to shut down");
                    }
                });
            }
        }));
        injector = Guice.createInjector(new ApplicationSupportModule(applicationLifecycleControl), moduleSupplier.get());
    }

    /**
     * Start all {@link LifecycleComponent}s.
     *
     * @throws InterruptedException if the thread was interrupted while starting
     * @throws RuntimeException     if one of the components failed to start; note components that are already started won't be stopped, use
     *                              {@link #stop()} for that.
     */
    public void start() throws InterruptedException {
        startedAllComponentsSuccessfully.set(false);
        componentsAttemptedToStart.clear();
        logger.info("Initialising components");
        List<LifecycleComponent> allComponents = injector
                .findBindingsByType(new TypeLiteral<LifecycleComponent>() {})
                .stream()
                .map(lifecycleComponentBinding -> lifecycleComponentBinding.getProvider().get())
                .collect(toImmutableList());

        logger.info("Starting components");
        for (LifecycleComponent component : allComponents) {
            if (Thread.interrupted()) {
                throw new InterruptedException(String.format("Interrupted while starting; components attempted to start: %s out of %s",
                        componentsAttemptedToStart.size(), allComponents.size()));
            }
            componentsAttemptedToStart.add(component);
            start(component);
        }

        startedAllComponentsSuccessfully.set(true);
        logger.info("Started");
    }

    public Injector getInjector() {
        return injector;
    }

    /**
     * Stop all components that have been started - must be called on same thread that called {@link #start()}.
     */
    public void stop() {
        logger.info("Shutting down");
        stop(componentsAttemptedToStart);
        componentsAttemptedToStart.clear();
        logger.info("Shut down");
    }

    /**
     * Run as a daemon: start and blocks until application initiates shutdown (or JVM is requested to shut down) and all components are stopped.
     */
    public void run() {
        checkState(runCalled.compareAndSet(false, true), "Application.run() can only be called once");
        runThread = Thread.currentThread();
        do {
            logger.info("Starting");

            shutdownLatch = new CountDownLatch(1);
            fullyStoppedLatch = new CountDownLatch(1);

            try {
                start();
            } catch (InterruptedException | RuntimeException e) {
                logger.error("Unable to initialize", e);
                shutdownLatch.countDown();
                // intentionally clearing the interrupted flag to guarantee the immediately following latch await not to fail
                //noinspection ResultOfMethodCallIgnored
                Thread.interrupted();
            }

            MoreThrowables.asUnchecked(shutdownLatch::await);
            stop();

            fullyStoppedLatch.countDown();
        } while (restarting.getAndSet(false));
    }

    public static Builder builder() {
        return new Builder();
    }

    private void initiateStop() {
        if (startedAllComponentsSuccessfully.get()) {
            shutdownLatch.countDown();
        } else {
            logger.info("Interrupting startup sequence");
            runThread.interrupt();
        }
    }

    private static void start(LifecycleComponent lifecycleComponent) {
        logger.info("Starting component {}", lifecycleComponent.name());
        lifecycleComponent.start();
        logger.info("Started component {}", lifecycleComponent.name());
    }

    private static void stop(List<LifecycleComponent> lifecycleComponents) {
        Lists.reverse(lifecycleComponents).forEach(lifecycleComponent -> {
            try {
                logger.info("Stopping component {}", lifecycleComponent.name());
                lifecycleComponent.stop();
                logger.info("Stopped component {}", lifecycleComponent.name());
            } catch (Throwable e) {
                logger.error("Failed stopping component {}", lifecycleComponent.name(), e);
            }
        });
    }

    public static final class Builder implements TypedBuilder<Application> {
        private final ImmutableList.Builder<Supplier<Module>> moduleSupplierListBuilder = ImmutableList.builder();

        public Builder addModule(Supplier<Module> moduleSupplier) {
            moduleSupplierListBuilder.add(moduleSupplier);
            return this;
        }

        @Override
        public Application build() {
            List<Supplier<Module>> moduleSuppliers = moduleSupplierListBuilder.build();
            Module module = new AbstractModule() {
                @Override
                protected void configure() {
                    moduleSuppliers.stream()
                            .map(Supplier::get)
                            .forEach(this::install);
                }
            };
            return new Application(() -> module);
        }
    }
}
