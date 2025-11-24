package net.yudichev.jiotty.connector.tesla.fleet;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import net.yudichev.jiotty.common.app.Application;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.keystore.KeyStoreAccessModule;
import net.yudichev.jiotty.common.lang.MoreThrowables;
import net.yudichev.jiotty.common.net.SslCustomisationModule;
import net.yudichev.jiotty.common.time.TimeModule;
import net.yudichev.jiotty.common.varstore.VarStoreModule;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.exposedBy;
import static net.yudichev.jiotty.common.inject.BindingSpec.literally;
import static net.yudichev.jiotty.common.keystore.KeyStoreEntryModule.keyStoreEntry;
import static net.yudichev.jiotty.common.lang.HumanReadableExceptionMessage.humanReadableMessage;
import static net.yudichev.jiotty.connector.tesla.fleet.TeslaVehicle.Endpoint.CHARGE_STATE;
import static net.yudichev.jiotty.connector.tesla.fleet.TeslaVehicle.Endpoint.CLIMATE_STATE;
import static net.yudichev.jiotty.connector.tesla.fleet.TeslaVehicle.Endpoint.LOCATION_DATA;
import static net.yudichev.jiotty.connector.tesla.fleet.TeslaVehicle.Endpoint.VEHICLE_STATE;

final class TeslaFleetManualRunner {

    private static String vin;

    public static void main(String[] args) {
        vin = checkNotNull(args[4]);
        Application.builder()
                   .addModule(() -> VarStoreModule.builder().setPath(literally(Paths.get(args[0]))).build())
                   .addModule(TimeModule::new)
                   .addModule(() -> KeyStoreAccessModule.builder()
                                                        .setPathToKeystore(literally(args[7]).map(new TypeToken<>() {}, new TypeToken<>() {}, Paths::get))
                                                        .setKeystorePass(literally(args[8]))
                                                        .build())
                   .addModule(() -> TeslaFleetModule.builder()
                                                    .setClientId(literally(args[1]))
                                                    .setClientSecret(literally(args[2]))
                                                    .withBaseUrl(literally(args[3]))
                                                    .withSslCustomisation(exposedBy(SslCustomisationModule.builder()
                                                                                                          .setTrustStorePath(literally(Paths.get(args[5])))
                                                                                                          .setTrustStorePassword(keyStoreEntry(args[6]))
                                                                                                          .build()))
                                                    .build())
                   .addModule(() -> new BaseLifecycleComponentModule() {
                       @Override
                       protected void configure() {
                           registerLifecycleComponent(CmdLineTest.class);
                       }
                   })
                   .build()
                   .run();
    }

    @SuppressWarnings({"UseOfSystemOutOrSystemErr", "OverlyBroadCatchBlock", "CallToPrintStackTrace"})
    private static class CmdLineTest extends BaseLifecycleComponent {
        private final TeslaFleet teslaFleet;

        @Inject
        public CmdLineTest(TeslaFleet teslaFleet) {
            this.teslaFleet = teslaFleet;
        }

        @Override
        protected void doStart() {
            teslaFleet.listVehicles().whenComplete((teslaVehicles, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                } else {
                    processVehicles(teslaVehicles);
                }
            });
        }

        private void processVehicles(List<TeslaVehicleData> teslaVehicles) {
            System.out.println();
            for (TeslaVehicleData teslaVehicle : teslaVehicles) {
                System.out.println(teslaVehicle);
            }
            TeslaVehicleData vehicle = teslaVehicles.stream().filter(teslaVehicle -> teslaVehicle.vin().equals(vin)).findFirst().orElseThrow();
            TeslaVehicle car = teslaFleet.vehicle(vehicle.vin());
            new Thread(() -> {
                System.out.println("ENTER COMMAND (get, wake, setlim <lim>, chstart, chstop, constart, constop):");
                var reader = new BufferedReader(new InputStreamReader(System.in));
                String line;
                while ((line = MoreThrowables.getAsUnchecked(reader::readLine)) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        try {
                            String[] cmd = line.split("\\W+");
                            var future = switch (cmd[0]) {
                                case "setlim" -> car.setChargeLimit(Integer.parseInt(cmd[1]));
                                case "chstart" -> car.startCharge();
                                case "chstop" -> car.stopCharge();
                                case "constart" -> car.startAutoConditioning();
                                case "constop" -> car.stopAutoConditioning();
                                case "wake" -> car.wakeUp();
                                case "get" -> car.getData(ImmutableSet.of(CHARGE_STATE, VEHICLE_STATE, LOCATION_DATA, CLIMATE_STATE));
                                default -> {
                                    System.err.println("What is this - " + Arrays.toString(cmd));
                                    yield CompletableFuture.completedFuture(null);
                                }
                            };
                            Object result = future.get(20, TimeUnit.SECONDS);
                            System.out.println(result == null ? "Done" : "Result: " + result);
                        } catch (Exception e) {
                            System.out.println("Failure: " + humanReadableMessage(e));
                        }
                    }
                }
            }).start();

        }
    }
}