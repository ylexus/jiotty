package net.yudichev.jiotty.connector.homeassistant;

import net.yudichev.jiotty.common.app.Application;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.literally;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;

final class HomeAssistantClientLocalRunner {
    public static void main(String[] args) {
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
        public static final Pattern PATTERN = Pattern.compile(
                "^\\s*(\\S+)\\s+(\\S+)\\s+(\\S+)(?:\\s+(\\S+))?(?:\\s+(\\S+))?(?:\\s+(\\S+))?(?:\\s+(\\S+))?(?:\\s+(\\S+))?(?:\\s+(\\S+))?(?:\\s+(\\S+))?(?:\\s+(\\S+))?\\s*$");
        private final HomeAssistantClient client;
        private Thread thread;

        @Inject
        public Runner(HomeAssistantClient client) {
            this.client = checkNotNull(client);
        }

        @SuppressWarnings({"NestedSwitchStatement", "UseOfSystemOutOrSystemErr", "ThrowCaughtLocally", "OverlyBroadCatchBlock", "OverlyComplexMethod",
                "OverlyLongMethod", "SwitchStatementWithTooFewBranches", "OverlyNestedMethod"})
        @Override
        protected void doStart() {
            thread = new Thread(() -> {
                var reader = new BufferedReader(new InputStreamReader(System.in));
                String line;
                while ((line = getAsUnchecked(reader::readLine)) != null) {
                    var matcher = PATTERN.matcher(line);
                    if (matcher.matches()) {
                        var domain = matcher.group(1);
                        var command = matcher.group(2);
                        var entity = matcher.group(3);
                        try {
                            CompletableFuture<?> result;
                            if ("get".equals(command)) {
                                result = (switch (domain) {
                                    case "climate" -> client.climate().getState(entity);
                                    case "number" -> client.number().getState(entity);
                                    case "switch" -> client.aSwitch().getState(entity);
                                    case "button" -> client.button().getState(entity);
                                    case "sensor" -> client.sensor().getState(entity);
                                    case "bsensor" -> client.binarySensor().getBinaryState(entity);
                                    default -> throw new IllegalArgumentException("Unrecognised domain: " + domain);
                                });
                            } else {
                                result = switch (domain) {
                                    case "climate" -> switch (command) {
                                        case "on" -> client.climate().turnOn("climate." + entity);
                                        case "off" -> client.climate().turnOff("climate." + entity);
                                        default -> throw new IllegalArgumentException("Unrecognised climate command: " + command);
                                    };
                                    case "number" -> {
                                        double value = Double.parseDouble(matcher.group(4));
                                        yield switch (command) {
                                            case "set" -> client.number().setValue("number." + entity, value);
                                            default -> throw new IllegalArgumentException("Unrecognised number command: " + command);
                                        };
                                    }
                                    case "switch" -> switch (command) {
                                        case "on" -> client.aSwitch().turnOn("switch." + entity);
                                        case "off" -> client.aSwitch().turnOff("switch." + entity);
                                        default -> throw new IllegalArgumentException("Unrecognised switch command: " + command);
                                    };
                                    case "button" -> switch (command) {
                                        case "press" -> client.button().press("switch." + entity);
                                        default -> throw new IllegalArgumentException("Unrecognised switch command: " + command);
                                    };
                                    default -> throw new IllegalArgumentException("Unrecognised domain: " + domain);
                                };
                            }
                            System.out.println("Awaiting Result...");
                            System.out.println("Result: " + getAsUnchecked(result::get));
                        } catch (RuntimeException e) {
                            System.err.println("Failed: " + e.getMessage());
                        }
                    } else {
                        System.err.println("Unparseable command: " + line);
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