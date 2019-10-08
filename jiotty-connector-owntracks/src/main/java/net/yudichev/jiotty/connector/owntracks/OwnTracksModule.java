package net.yudichev.jiotty.connector.owntracks;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponentModule;
import net.yudichev.jiotty.common.inject.ExposedKeyModule;
import net.yudichev.jiotty.common.lang.TypedBuilder;

public final class OwnTracksModule extends BaseLifecycleComponentModule implements ExposedKeyModule<OwnTracks> {
    private OwnTracksModule() {
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void configure() {
        bind(getExposedKey()).to(OwnTracksImpl.class);
        expose(getExposedKey());
    }

    public static class Builder implements TypedBuilder<ExposedKeyModule<OwnTracks>> {
        @Override
        public ExposedKeyModule<OwnTracks> build() {
            return new OwnTracksModule();
        }
    }
}
