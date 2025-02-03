package net.yudichev.jiotty.connector.octopusenergy.agilepredict;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;

public final class AgilePredictPriceModule extends BaseLifecycleComponentModule implements ExposedKeyModule<AgilePredictPriceService> {

    @Override
    protected void configure() {
        bind(getExposedKey()).to(registerLifecycleComponent(AgilePredictPriceServiceImpl.class));
        expose(getExposedKey());
    }
}
