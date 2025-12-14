package net.yudichev.jiotty.common.graph;

public abstract class BaseTestNode implements Node {
    private NodeContext nodeContext;

    @Override
    public void initialise(NodeContext nodeContext) {
        this.nodeContext = nodeContext;
    }

    public NodeContext nodeContext() {
        return nodeContext;
    }

    public void triggerInNextWave() {
        nodeContext.triggerInNextWave();
    }

    @Override
    public boolean wave() {
        return true;
    }
}
