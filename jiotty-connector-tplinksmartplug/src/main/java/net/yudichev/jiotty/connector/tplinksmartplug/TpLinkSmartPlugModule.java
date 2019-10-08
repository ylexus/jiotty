package net.yudichev.jiotty.connector.tplinksmartplug;

import com.google.inject.Key;
import net.yudichev.jiotty.appliance.Appliance;
import net.yudichev.jiotty.appliance.ApplianceModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.inject.HasWithAnnotation;
import net.yudichev.jiotty.common.inject.SpecifiedAnnotation;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.SpecifiedAnnotation.forNoAnnotation;

public final class TpLinkSmartPlugModule extends ApplianceModule {
    private final String username;
    private final String password;
    private final String termId;
    private final String deviceId;

    private TpLinkSmartPlugModule(String username,
                                  String password,
                                  String termId,
                                  String deviceId,
                                  SpecifiedAnnotation targetAnnotation) {
        super(targetAnnotation);
        this.username = checkNotNull(username);
        this.password = checkNotNull(password);
        this.termId = checkNotNull(termId);
        this.deviceId = checkNotNull(deviceId);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected Key<? extends Appliance> configureDependencies() {
        bindConstant().annotatedWith(TpLinkSmartPlug.Username.class).to(username);
        bindConstant().annotatedWith(TpLinkSmartPlug.Password.class).to(password);
        bindConstant().annotatedWith(TpLinkSmartPlug.TermId.class).to(termId);
        bindConstant().annotatedWith(TpLinkSmartPlug.DeviceId.class).to(deviceId);
        return boundLifecycleComponent(TpLinkSmartPlug.class);
    }

    public static class Builder implements TypedBuilder<ExposedKeyModule<Appliance>>, HasWithAnnotation {
        private String username;
        private String password;
        private String termId;
        private String deviceId;
        private SpecifiedAnnotation specifiedAnnotation = forNoAnnotation();

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setTermId(String termId) {
            this.termId = termId;
            return this;
        }

        public Builder setDeviceId(String deviceId) {
            this.deviceId = checkNotNull(deviceId);
            return this;
        }

        @Override
        public Builder withAnnotation(SpecifiedAnnotation specifiedAnnotation) {
            this.specifiedAnnotation = checkNotNull(specifiedAnnotation);
            return this;
        }

        @Override
        public ExposedKeyModule<Appliance> build() {
            return new TpLinkSmartPlugModule(username, password, termId, deviceId, specifiedAnnotation);
        }
    }
}
