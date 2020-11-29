package net.yudichev.jiotty.connector.ir.lirc;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.literally;

public final class LircClientModule extends BaseLifecycleComponentModule implements ExposedKeyModule<LircClient> {
    private final BindingSpec<String> addressSpec;
    private final BindingSpec<Integer> portSpec;
    private final BindingSpec<Duration> timeoutSpec;

    private LircClientModule(BindingSpec<String> addressSpec, BindingSpec<Integer> portSpec, BindingSpec<Duration> timeoutSpec) {
        this.addressSpec = checkNotNull(addressSpec);
        this.portSpec = checkNotNull(portSpec);
        this.timeoutSpec = checkNotNull(timeoutSpec);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        addressSpec.bind(String.class)
                .annotatedWith(TcpLircClient.Address.class)
                .installedBy(this::installLifecycleComponentModule);
        portSpec.bind(Integer.class)
                .annotatedWith(TcpLircClient.Port.class)
                .installedBy(this::installLifecycleComponentModule);
        timeoutSpec.bind(Duration.class)
                .annotatedWith(TcpLircClient.Timeout.class)
                .installedBy(this::installLifecycleComponentModule);
        bind(getExposedKey()).to(boundLifecycleComponent(TcpLircClient.class));
        expose(getExposedKey());
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<LircClient>> {
        private BindingSpec<String> addressSpec = literally("127.0.0.1");
        private BindingSpec<Integer> portSpec = literally(8765);
        private BindingSpec<Duration> timeoutSpec = literally(Duration.ofSeconds(5));

        private Builder() {
        }

        public Builder withAddress(BindingSpec<String> addressSpec) {
            this.addressSpec = checkNotNull(addressSpec);
            return this;
        }

        public Builder withPort(BindingSpec<Integer> portSpec) {
            this.portSpec = portSpec;
            return this;
        }

        public Builder withTimeout(BindingSpec<Duration> timeoutSpec) {
            this.timeoutSpec = checkNotNull(timeoutSpec);
            return this;
        }

        @Override
        public ExposedKeyModule<LircClient> build() {
            return new LircClientModule(addressSpec, portSpec, timeoutSpec);
        }
    }
}
