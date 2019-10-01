package net.jiotty.connector.fieldglass;

import net.jiotty.common.inject.BaseLifecycleComponentModule;
import net.jiotty.common.inject.ExposedKeyModule;

public final class FieldglassTimeSheetsModule extends BaseLifecycleComponentModule implements ExposedKeyModule<FieldglassTimeSheetsClient> {
    @Override
    protected void configure() {
        bind(getExposedKey()).to(FieldglassTimeSheetsClientImpl.class);
        expose(getExposedKey());
    }
}
