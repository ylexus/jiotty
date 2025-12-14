package net.yudichev.jiotty.common.graph;

public interface NodeContext {
    NodeContext subscribeTo(Node node);

    Node node();

    String name();

    void triggerInNextWave();

    void triggerMeAndParentsInNextWave();

    Graph graph();
}
