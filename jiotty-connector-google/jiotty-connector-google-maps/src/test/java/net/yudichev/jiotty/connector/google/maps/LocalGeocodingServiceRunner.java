package net.yudichev.jiotty.connector.google.maps;

import net.yudichev.jiotty.common.app.Application;
import net.yudichev.jiotty.common.async.ExecutorModule;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static net.yudichev.jiotty.common.inject.BindingSpec.literally;

final class LocalGeocodingServiceRunner {
    private static final Logger logger = LoggerFactory.getLogger(LocalGeocodingServiceRunner.class);

    public static void main(String[] args) {
        Application.builder()
                   .addModule(ExecutorModule::new)
                   .addModule(() -> GeocodingServiceModule.builder()
                                                          .setApiKey(literally(args[0]))
                                                          .build())
                   .addModule(() -> new BaseLifecycleComponentModule() {
                       @Override
                       protected void configure() {
                           registerLifecycleComponent(Runner.class);
                       }
                   })
                   .build()
                   .run();
    }

    @SuppressWarnings("CallToSystemExit")
    static class Runner extends BaseLifecycleComponent {

        private final GeocodingService service;

        @Inject
        public Runner(GeocodingService service) {
            this.service = service;
        }

        @Override
        protected void doStart() {
            service.geocode("Westminster Abbey")
                   .whenComplete((results, throwable) -> {
                       logger.info("Results: {}", results, throwable);
                       var thread = new Thread(() -> System.exit(0));
                       thread.setDaemon(true);
                       thread.start();
                   });
        }
    }
}
