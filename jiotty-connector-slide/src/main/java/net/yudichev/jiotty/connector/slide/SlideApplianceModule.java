package net.yudichev.jiotty.connector.slide;

import com.google.inject.Key;
import net.yudichev.jiotty.appliance.Appliance;
import net.yudichev.jiotty.appliance.ApplianceModule;
import net.yudichev.jiotty.common.inject.BindingSpec;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.inject.HasWithAnnotation;
import net.yudichev.jiotty.common.inject.SpecifiedAnnotation;
import net.yudichev.jiotty.common.lang.TypedBuilder;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.inject.SpecifiedAnnotation.forNoAnnotation;
import static net.yudichev.jiotty.connector.slide.Bindings.SlideId;

public final class SlideApplianceModule extends ApplianceModule {
    private final BindingSpec<Long> slideIdSpec;
    private final BindingSpec<SlideService> slideServiceSpec;

    public SlideApplianceModule(BindingSpec<Long> slideIdSpec,
                                BindingSpec<SlideService> slideServiceSpec,
                                SpecifiedAnnotation specifiedAnnotation) {
        super(specifiedAnnotation);
        this.slideIdSpec = checkNotNull(slideIdSpec);
        this.slideServiceSpec = checkNotNull(slideServiceSpec);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected Key<? extends Appliance> configureDependencies() {
        slideIdSpec.bind(Long.class)
                .annotatedWith(SlideId.class)
                .installedBy(this::installLifecycleComponentModule);
        slideServiceSpec.bind(SlideService.class)
                .annotatedWith(SlideAsAppliance.Dependency.class)
                .installedBy(this::installLifecycleComponentModule);
        return Key.get(SlideAsAppliance.class);
    }

    public static final class Builder implements TypedBuilder<ExposedKeyModule<Appliance>>, HasWithAnnotation {
        private BindingSpec<SlideService> slideServiceSpec = BindingSpec.boundTo(SlideService.class);
        private BindingSpec<Long> slideIdSpec;
        private SpecifiedAnnotation specifiedAnnotation = forNoAnnotation();

        public Builder setSlideIdSpec(BindingSpec<Long> slideIdSpec) {
            this.slideIdSpec = checkNotNull(slideIdSpec);
            return this;
        }

        public Builder withSlideService(BindingSpec<SlideService> slideServiceSpec) {
            this.slideServiceSpec = checkNotNull(slideServiceSpec);
            return this;
        }

        @Override
        public Builder withAnnotation(SpecifiedAnnotation specifiedAnnotation) {
            this.specifiedAnnotation = checkNotNull(specifiedAnnotation);
            return this;
        }

        @Override
        public ExposedKeyModule<Appliance> build() {
            return new SlideApplianceModule(slideIdSpec, slideServiceSpec, specifiedAnnotation);
        }
    }
}
