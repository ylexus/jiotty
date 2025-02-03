package net.yudichev.jiotty.connector.octopusenergy.agilepredict;

import net.yudichev.jiotty.common.app.Application;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.lang.CompletableFutures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
final class AgilePredictPriceManualRunner {
    public static void main(String[] args) {
        Application.builder()
                   .addModule(AgilePredictPriceModule::new)
                   .addModule(() -> new BaseLifecycleComponentModule() {
                       @Override
                       protected void configure() {
                           registerLifecycleComponent(CmdLineTest.class);
                       }
                   })
                   .build()
                   .run();
    }

    private static class CmdLineTest extends BaseLifecycleComponent {
        private static final Logger logger = LoggerFactory.getLogger(CmdLineTest.class);

        private final AgilePredictPriceService service;

        @Inject
        public CmdLineTest(AgilePredictPriceService service) {
            this.service = service;
        }

        @Override
        protected void doStart() {
            service.getPrices("C", 14).whenComplete(CompletableFutures.logErrorOnFailure(logger, "error"))
                   .thenAccept(prices -> System.out.println("PRICES: " + prices));
        }
    }
}