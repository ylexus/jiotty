package net.yudichev.jiotty.connector.homeassistant;

import net.yudichev.jiotty.common.app.Application;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.literally;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;

final class HomeAssistantClientLocalRunner {
    private static String entityId;

    public static void main(String[] args) {
        entityId = args[2];
        Application.builder()
                   .addModule(() -> HomeAssistantClientModule
                           .builder()
                           .setBaseUrlSpec(literally(args[0]))
                           .setAccessTokenSpec(literally(args[1]))
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

    static class Runner extends BaseLifecycleComponent {
        private final HomeAssistantClient client;
        private Thread thread;

        @Inject
        public Runner(HomeAssistantClient client) {
            this.client = checkNotNull(client);
        }

        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        @Override
        protected void doStart() {
            thread = new Thread(() -> {
                var reader = new BufferedReader(new InputStreamReader(System.in));
                String line;
                while ((line = getAsUnchecked(reader::readLine)) != null) {
                    line = line.trim();
                    switch (line) {
                        case "on" -> client.climate().turnOn("climate.thermostat");
                        case "off" -> client.climate().turnOff(entityId);
                        default -> System.err.println("Unknown command: " + line);
                    }
                }
            });
            thread.start();
        }

        @Override
        protected void doStop() {
            if (thread != null) {
                thread.interrupt();
            }
        }
    }
}
