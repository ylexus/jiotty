package net.yudichev.jiotty.common.async;

import com.google.inject.Key;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;

import java.lang.annotation.Annotation;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ExecutorProviderModule extends BaseLifecycleComponentModule implements ExposedKeyModule<SchedulingExecutor> {
    private final String threadName;
    private final Key<SchedulingExecutor> exposedKey;

    public ExecutorProviderModule(String threadName, Class<? extends Annotation> annotation) {
        this.threadName = checkNotNull(threadName);
        exposedKey = ExposedKeyModule.super.getExposedKey().withAnnotation(annotation);
    }

    @Override
    public Key<SchedulingExecutor> getExposedKey() {
        return exposedKey;
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(ExecutorProvider.ThreadName.class).to(threadName);
        bind(exposedKey).toProvider(registerLifecycleComponent(ExecutorProvider.class));
        expose(exposedKey);
    }
}
