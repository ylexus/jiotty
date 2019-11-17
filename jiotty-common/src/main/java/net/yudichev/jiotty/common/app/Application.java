package net.yudichev.jiotty.common.app;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

public final class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
    }

    private final Supplier<Module> moduleSupplier;
    private final List<LifecycleComponent> componentsAttemptedToStart = new CopyOnWriteArrayList<>();
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private final CountDownLatch fullyStoppedLatch = new CountDownLatch(1);
    private final ApplicationLifecycleControl applicationLifecycleControl;
    private final AtomicBoolean startedAllComponentsSuccessfully = new AtomicBoolean();
    private final AtomicBoolean runCalled = new AtomicBoolean();
    private Thread runThread;

    private Application(Supplier<Module> moduleSupplier) {
        this.moduleSupplier = checkNotNull(moduleSupplier);
        applicationLifecycleControl = () -> {
            logger.info("Application requested shutdown");
            initiateStop();
        };
    }

    public void run() {
        checkState(!runCalled.getAndSet(true), "Application.run() can only be called once");
        logger.info("Starting");
        try {
            runThread = Thread.currentThread();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown hook fired");
                initiateStop();
                MoreThrowables.asUnchecked(() -> {
                    if (!fullyStoppedLatch.await(1, TimeUnit.MINUTES)) {
                        logger.warn("Timed out waiting for partially initialised application to shut down");
                    }
                });
            }));

            logger.info("Initialising components");
            List<LifecycleComponent> allComponents = Guice.createInjector(new ApplicationSupportModule(applicationLifecycleControl), moduleSupplier.get())
                    .findBindingsByType(new TypeLiteral<LifecycleComponent>() {})
                    .stream()
                    .map(lifecycleComponentBinding -> lifecycleComponentBinding.getProvider().get())
                    .collect(toImmutableList());

            logger.info("Starting components");
            for (LifecycleComponent component : allComponents) {
                if (Thread.interrupted()) {
                    //noinspection ThrowCaughtLocally
                    throw new InterruptedException(String.format("Interrupted while starting; components attempted to start: %s out of %s",
                            componentsAttemptedToStart.size(), allComponents.size()));
                }
                componentsAttemptedToStart.add(component);
                start(component);
            }

            startedAllComponentsSuccessfully.set(true);
            logger.info("Started");

        } catch (InterruptedException | RuntimeException e) {
            logger.error("Unable to initialize", e);
            shutdownLatch.countDown();
        }

        MoreThrowables.asUnchecked(shutdownLatch::await);
        doStop();

        fullyStoppedLatch.countDown();
    }

    public void doStop() {
        logger.info("Shutting down");
        stop(componentsAttemptedToStart);
        logger.info("Shut down");
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
