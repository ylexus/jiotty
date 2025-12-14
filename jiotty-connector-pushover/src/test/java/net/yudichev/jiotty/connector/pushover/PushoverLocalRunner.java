package net.yudichev.jiotty.connector.pushover;

import jakarta.inject.Inject;
import net.pushover.client.MessagePriority;
import net.yudichev.jiotty.common.app.Application;
import net.yudichev.jiotty.common.async.ExecutorModule;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.keystore.KeyStoreEntryModule.keyStoreEntry;

final class PushoverLocalRunner {
    private static String[] args;

    static void main(String[] args) {
        PushoverLocalRunner.args = args;
        Application.builder()
                   .addModule(ExecutorModule::new)
                   .addModule(() -> new PushoverUserAlerterModule(keyStoreEntry("pushover-api-token")))
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
        private final net.yudichev.jiotty.connector.pushover.UserAlerter userAlerter;
        private Thread thread;

        @Inject
        public Runner(net.yudichev.jiotty.connector.pushover.UserAlerter userAlerter) {
            this.userAlerter = checkNotNull(userAlerter);
        }

        @Override
        protected void doStart() {
            thread = new Thread(() -> userAlerter.sendAlert(() -> args[0], MessagePriority.EMERGENCY, "Ze Alert"));
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
