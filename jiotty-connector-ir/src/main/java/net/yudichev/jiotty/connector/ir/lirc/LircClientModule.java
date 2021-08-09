package net.yudichev.jiotty.connector.ir.lirc;

import net.yudichev.jiotty.common.async.backoff.BackOffConfig;
import net.yudichev.jiotty.common.async.backoff.BackingOffExceptionHandlerModule;
import net.yudichev.jiotty.common.async.backoff.RetryableOperationExecutorModule;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.*;
import static net.yudichev.jiotty.common.inject.SpecifiedAnnotation.forAnnotation;

public final class LircClientModule extends BaseLifecycleComponentModule implements ExposedKeyModule<LircClient> {
    private final BindingSpec<String> addressSpec;
    private final BindingSpec<Integer> portSpec;
    private final BindingSpec<Duration> timeoutSpec;
    private final BindingSpec<BackOffConfig> retryBackoffConfigSpec;

    private LircClientModule(BindingSpec<String> addressSpec, BindingSpec<Integer> portSpec, BindingSpec<Duration> timeoutSpec, BindingSpec<BackOffConfig> retryBackoffConfigSpec) {
        this.addressSpec = checkNotNull(addressSpec);
        this.portSpec = checkNotNull(portSpec);
        this.timeoutSpec = checkNotNull(timeoutSpec);
        this.retryBackoffConfigSpec = checkNotNull(retryBackoffConfigSpec);
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
        installLifecycleComponentModule(RetryableOperationExecutorModule.builder()
                .setBackingOffExceptionHandler(exposedBy(BackingOffExceptionHandlerModule.builder()
                        .setRetryableExceptionPredicate(boundTo(LircRetryableExceptionPredicate.class))
                        .withConfig(retryBackoffConfigSpec)
                        .build()))
                .withAnnotation(forAnnotation(TcpLircClient.Dependency.class))
                .build());
        bind(getExposedKey()).to(registerLifecycleComponent(TcpLircClient.class));
        expose(getExposedKey());
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<LircClient>> {
        private BindingSpec<String> addressSpec = literally("127.0.0.1");
        private BindingSpec<Integer> portSpec = literally(8765);
        private BindingSpec<Duration> timeoutSpec = literally(Duration.ofSeconds(5));
        private BindingSpec<BackOffConfig> retryBackoffConfigSpec = literally(BackOffConfig.builder()
                .setInitialInterval(Duration.ofMillis(10))
                .setMaxInterval(Duration.ofMinutes(1))
                .setMaxElapsedTime(Duration.ofDays(1))
                .build());

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

        public Builder withRetryBackoffConfig(BindingSpec<BackOffConfig> retryBackoffConfigSpec) {
            this.retryBackoffConfigSpec = checkNotNull(retryBackoffConfigSpec);
            return this;
        }

        @Override
        public ExposedKeyModule<LircClient> build() {
            return new LircClientModule(addressSpec, portSpec, timeoutSpec, retryBackoffConfigSpec);
        }
    }
}
