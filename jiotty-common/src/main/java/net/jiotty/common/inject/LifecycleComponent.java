package net.jiotty.common.inject;

import net.jiotty.common.lang.HasName;

public interface LifecycleComponent extends HasName {
    void start();

    void stop();
}
