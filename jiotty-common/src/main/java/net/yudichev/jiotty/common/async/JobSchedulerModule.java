package net.yudichev.jiotty.common.async;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import java.time.ZoneId;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.BindingSpec.literally;

public final class JobSchedulerModule extends BaseLifecycleComponentModule implements ExposedKeyModule<JobScheduler> {
    private final BindingSpec<ZoneId> zoneIdSpec;

    private JobSchedulerModule(BindingSpec<ZoneId> zoneIdSpec) {
        this.zoneIdSpec = checkNotNull(zoneIdSpec);
    }

    @Override
    protected void configure() {
        zoneIdSpec.bind(ZoneId.class).annotatedWith(JobSchedulerImpl.Dependency.class).installedBy(this::installLifecycleComponentModule);
        bind(getExposedKey()).to(registerLifecycleComponent(JobSchedulerImpl.class));
        expose(getExposedKey());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<JobScheduler>> {
        private BindingSpec<ZoneId> zoneIdSpec = literally(ZoneId.systemDefault());

        public Builder withZoneId(BindingSpec<ZoneId> zoneIdSpec) {
            this.zoneIdSpec = checkNotNull(zoneIdSpec);
            return this;
        }

        @Override
        public ExposedKeyModule<JobScheduler> build() {
            return new JobSchedulerModule(zoneIdSpec);
        }
    }
}
