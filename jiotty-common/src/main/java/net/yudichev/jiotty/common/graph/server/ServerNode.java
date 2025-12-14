package net.yudichev.jiotty.common.graph.server;

import net.yudichev.jiotty.common.graph.Node;

public interface ServerNode extends Node {
    void registerInGraph();

    void logState(String when);
}
