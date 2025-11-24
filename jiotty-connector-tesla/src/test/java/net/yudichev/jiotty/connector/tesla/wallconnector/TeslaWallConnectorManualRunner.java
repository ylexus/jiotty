package net.yudichev.jiotty.connector.tesla.wallconnector;

import net.yudichev.jiotty.common.app.Application;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.literally;

@SuppressWarnings("CallToPrintStackTrace")
class TeslaWallConnectorManualRunner {
    public static void main(String[] args) {
        Application.builder()
                   .addModule(() -> TeslaWallConnectorModule.builder()
                                                            .setHostAddress(literally(args[0]))
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

    private static class Runner extends BaseLifecycleComponent {
        private final TeslaWallConnector teslaWallConnector;

        @Inject
        public Runner(TeslaWallConnector teslaWallConnector) {
            this.teslaWallConnector = checkNotNull(teslaWallConnector);
        }

        @Override
        protected void doStart() {
            new Thread(() -> teslaWallConnector.getVitals().whenComplete((vitals, e) -> {
                if (e != null) {
                    e.printStackTrace();
                } else {
                    System.out.println("RESULT: " + vitals);
                }
                new Thread(() -> System.exit(0)).start();
            })).start();
        }
    }
}