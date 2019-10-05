package net.jiotty.connector.fieldglass;

import net.jiotty.common.inject.BaseLifecycleComponentModule;
import net.jiotty.common.inject.ExposedKeyModule;
import net.jiotty.common.lang.TypedBuilder;

public final class FieldglassTimeSheetsModule extends BaseLifecycleComponentModule implements ExposedKeyModule<FieldglassTimeSheetsClient> {
    private FieldglassTimeSheetsModule() {
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        bind(getExposedKey()).to(FieldglassTimeSheetsClientImpl.class);
        expose(getExposedKey());
    }

    public static class Builder implements TypedBuilder<ExposedKeyModule<FieldglassTimeSheetsClient>> {
        @Override
        public ExposedKeyModule<FieldglassTimeSheetsClient> build() {
            return new FieldglassTimeSheetsModule();
        }
    }
}
