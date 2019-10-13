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
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();
    }

    private final Supplier<Module> moduleSupplier;

    private Application(Supplier<Module> moduleSupplier) {
        this.moduleSupplier = checkNotNull(moduleSupplier);
    }

    public void run() {
        CountDownLatch shutdownLatch = new CountDownLatch(1);
        List<LifecycleComponent> lifecycleComponents = new ArrayList<>();
        try {
            ApplicationLifecycleControl applicationLifecycleControl = () -> {
                logger.info("Application requested shutdown");
                shutdownLatch.countDown();
            };

            logger.info("Starting");
            Guice.createInjector(new ApplicationSupportModule(applicationLifecycleControl), moduleSupplier.get())
                    .findBindingsByType(new TypeLiteral<LifecycleComponent>() {})
                    .stream()
                    .map(lifecycleComponentBinding -> lifecycleComponentBinding.getProvider().get())
                    .forEach(lifecycleComponents::add);

            lifecycleComponents.forEach(Application::start);
            logger.info("Started");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown hook fired");
                shutdownLatch.countDown();
            }));
        } catch (RuntimeException e) {
            logger.error("Unable to initialize", e);
            shutdownLatch.countDown();
        }

        MoreThrowables.asUnchecked(shutdownLatch::await);
        logger.info("Shutting down");
        stop(lifecycleComponents);
        logger.info("Shut down");
        LogManager.shutdown();
    }

    public static Builder builder() {
        return new Builder();
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
