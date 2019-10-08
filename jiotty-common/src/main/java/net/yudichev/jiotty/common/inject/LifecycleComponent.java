package net.yudichev.jiotty.common.inject;

import net.yudichev.jiotty.common.lang.HasName;

public interface LifecycleComponent extends HasName {
    void start();

    void stop();
}
