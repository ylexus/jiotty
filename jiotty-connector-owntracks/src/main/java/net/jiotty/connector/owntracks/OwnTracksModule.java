package net.jiotty.connector.owntracks;

import net.jiotty.common.inject.BaseLifecycleComponentModule;
import net.jiotty.common.inject.ExposedKeyModule;

public final class OwnTracksModule extends BaseLifecycleComponentModule implements ExposedKeyModule<OwnTracks> {
    @Override
    protected void configure() {
        bind(getExposedKey()).to(OwnTracksImpl.class);
        expose(getExposedKey());
    }
}
