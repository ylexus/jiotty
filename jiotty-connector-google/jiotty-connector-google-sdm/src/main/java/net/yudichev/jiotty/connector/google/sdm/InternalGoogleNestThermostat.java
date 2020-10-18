package net.yudichev.jiotty.connector.google.sdm;

import com.google.api.services.smartdevicemanagement.v1.SmartDeviceManagement;
import com.google.api.services.smartdevicemanagement.v1.model.GoogleHomeEnterpriseSdmV1Device;
import com.google.api.services.smartdevicemanagement.v1.model.GoogleHomeEnterpriseSdmV1ExecuteDeviceCommandRequest;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;

final class InternalGoogleNestThermostat implements GoogleNestThermostat {
    private final SmartDeviceManagement smartDeviceManagement;
    private final String deviceName;

    InternalGoogleNestThermostat(SmartDeviceManagement smartDeviceManagement, String projectId, String deviceId) {
        this.smartDeviceManagement = checkNotNull(smartDeviceManagement);
        deviceName = "enterprises/" + projectId + "/devices/" + deviceId;
    }


    @Override
    public CompletableFuture<Mode> getCurrentMode() {
        return CompletableFuture.supplyAsync(() -> getAsUnchecked(() -> {
            GoogleHomeEnterpriseSdmV1Device device = smartDeviceManagement.enterprises().devices().get(deviceName).execute();
            @SuppressWarnings("unchecked")
            Map<String, String> modeTrait = (Map<String, String>) device.getTraits().get("sdm.devices.traits.ThermostatMode");
            checkState(modeTrait != null, "Thermostat does not support modes");
            @SuppressWarnings("unchecked")
            Map<String, String> ecoTrait = (Map<String, String>) device.getTraits().get("sdm.devices.traits.ThermostatEco");
            if (ecoTrait != null) {
                if ("MANUAL_ECO".equals(ecoTrait.get("mode"))) {
                    return Mode.ECO;
                }
            }
            return Mode.valueOf(modeTrait.get("mode"));
        }));
    }

    @Override
    public CompletableFuture<Void> setMode(Mode mode) {
        return CompletableFuture.supplyAsync(() -> getAsUnchecked(() -> {
            GoogleHomeEnterpriseSdmV1ExecuteDeviceCommandRequest request = new GoogleHomeEnterpriseSdmV1ExecuteDeviceCommandRequest();
            if (mode == Mode.ECO) {
                request.setCommand("sdm.devices.commands.ThermostatEco.SetMode");
                request.setParams(ImmutableMap.of("mode", "MANUAL_ECO"));
            } else {
                request.setCommand("sdm.devices.commands.ThermostatMode.SetMode");
                request.setParams(ImmutableMap.of("mode", mode.name()));
            }
            smartDeviceManagement.enterprises().devices().executeCommand(deviceName, request).execute();
            return null;
        }));
    }
}
