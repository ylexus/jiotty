package net.yudichev.jiotty.connector.ip;

import com.google.inject.Key;
import net.yudichev.jiotty.common.inject.*;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;

public final class HostMonitorModule extends BaseLifecycleComponentModule implements ExposedKeyModule<HostMonitor> {
    private final BindingSpec<String> hostnameSpec;
    private final BindingSpec<String> nameSpec;
    private final BindingSpec<Duration> toleranceSpec;
    private final Key<HostMonitor> exposedKey;

    private HostMonitorModule(BindingSpec<String> hostnameSpec,
                              BindingSpec<String> nameSpec,
                              BindingSpec<Duration> toleranceSpec,
                              SpecifiedAnnotation specifiedAnnotation) {
        this.hostnameSpec = checkNotNull(hostnameSpec);
        this.nameSpec = nameSpec;
        this.toleranceSpec = checkNotNull(toleranceSpec);
        exposedKey = specifiedAnnotation.specify(ExposedKeyModule.super.getExposedKey().getTypeLiteral());
    }

    @Override
    public Key<HostMonitor> getExposedKey() {
        return exposedKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        hostnameSpec.bind(String.class)
                .annotatedWith(HostMonitorImpl.Hostname.class)
                .installedBy(this::installLifecycleComponentModule);
        nameSpec.bind(String.class)
                .annotatedWith(HostMonitorImpl.Name.class)
                .installedBy(this::installLifecycleComponentModule);
        toleranceSpec.bind(Duration.class)
                .annotatedWith(HostMonitorImpl.Tolerance.class)
                .installedBy(this::installLifecycleComponentModule);
        bind(exposedKey).to(boundLifecycleComponent(HostMonitorImpl.class));
        expose(exposedKey);
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<HostMonitor>>, HasWithAnnotation {
        private BindingSpec<String> hostnameSpec;
        private BindingSpec<String> nameSpec;
        private BindingSpec<Duration> toleranceSpec = BindingSpec.literally(Duration.ofSeconds(30));
        private SpecifiedAnnotation specifiedAnnotation = SpecifiedAnnotation.forNoAnnotation();

        public Builder setHostname(BindingSpec<String> hostnameSpec) {
            this.hostnameSpec = checkNotNull(hostnameSpec);
            return this;
        }

        public Builder withName(BindingSpec<String> nameSpec) {
            this.nameSpec = checkNotNull(nameSpec);
            return this;
        }

        public Builder withTolerance(BindingSpec<Duration> toleranceSpec) {
            this.toleranceSpec = checkNotNull(toleranceSpec);
            return this;
        }

        @Override
        public Builder withAnnotation(SpecifiedAnnotation specifiedAnnotation) {
            this.specifiedAnnotation = checkNotNull(specifiedAnnotation);
            return this;
        }

        @Override
        public ExposedKeyModule<HostMonitor> build() {
            return new HostMonitorModule(hostnameSpec, nameSpec == null ? hostnameSpec : nameSpec, toleranceSpec, specifiedAnnotation);
        }
    }
}
