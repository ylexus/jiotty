package net.yudichev.jiotty.common.graph;

public interface NodeContext {
    NodeContext subscribeTo(Node node);

    Node node();

    String name();

    /// @return `true` if the node was not already pending trigger
    boolean triggerInNextWave();

    /// @return `true` if at least one node was not already pending trigger
    boolean triggerMeAndParentsInNextWave();

    Graph graph();
}
