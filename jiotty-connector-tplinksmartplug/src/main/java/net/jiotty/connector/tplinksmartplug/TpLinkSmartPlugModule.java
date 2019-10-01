package net.jiotty.connector.tplinksmartplug;

import com.google.inject.Key;
import net.jiotty.appliance.Appliance;
import net.jiotty.appliance.ApplianceModule;

import java.lang.annotation.Annotation;

import static com.google.common.base.Preconditions.checkNotNull;

public class TpLinkSmartPlugModule extends ApplianceModule {
    private final String username;
    private final String password;
    private final String termId;
    private final String deviceId;

    public TpLinkSmartPlugModule(String username,
                                 String password,
                                 String termId,
                                 String deviceId,
                                 Class<? extends Annotation> targetAnnotation) {
        super(targetAnnotation);
        this.username = checkNotNull(username);
        this.password = checkNotNull(password);
        this.termId = checkNotNull(termId);
        this.deviceId = checkNotNull(deviceId);
    }

    @Override
    protected final Key<? extends Appliance> configureDependencies() {
        bindConstant().annotatedWith(TpLinkSmartPlug.Username.class).to(username);
        bindConstant().annotatedWith(TpLinkSmartPlug.Password.class).to(password);
        bindConstant().annotatedWith(TpLinkSmartPlug.TermId.class).to(termId);
        bindConstant().annotatedWith(TpLinkSmartPlug.DeviceId.class).to(deviceId);
        return boundLifecycleComponent(TpLinkSmartPlug.class);
    }
}
