package net.yudichev.jiotty.common.graph.server;

import net.yudichev.jiotty.common.graph.BaseTestNode;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class BaseTestServerNode extends BaseTestNode implements ServerNode {
    private final GraphRunner graphRunner;

    protected BaseTestServerNode(GraphRunner graphRunner) {
        this.graphRunner = checkNotNull(graphRunner);
    }

    @Override
    public void registerInGraph() {
        graphRunner.graph().registerNode(getClass().getSimpleName(), this);
    }

    @Override
    public void logState(String when) {
    }
}
