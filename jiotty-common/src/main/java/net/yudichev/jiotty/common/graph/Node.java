package net.yudichev.jiotty.common.graph;

import net.yudichev.jiotty.common.lang.Closeable;

public interface Node extends Closeable {
    void initialise(NodeContext nodeContext);

    boolean wave();

    default void afterWave() {
    }

    @Override
    default void close() {
    }
}
