package net.jiotty.connector.world;

import net.jiotty.common.inject.BaseLifecycleComponentModule;
import net.jiotty.common.inject.ExposedKeyModule;
import net.jiotty.common.lang.TypedBuilder;

// TODO add HasWithAnnotation to all jiotty modules
public final class SunriseSunsetTimesModule extends BaseLifecycleComponentModule implements ExposedKeyModule<SunriseSunsetTimes> {
    private SunriseSunsetTimesModule() {
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        bind(getExposedKey()).to(SunriseSunsetTimesImpl.class);
        expose(getExposedKey());
    }

    public static class Builder implements TypedBuilder<ExposedKeyModule<SunriseSunsetTimes>> {
        @Override
        public ExposedKeyModule<SunriseSunsetTimes> build() {
            return new SunriseSunsetTimesModule();
        }
    }
}
