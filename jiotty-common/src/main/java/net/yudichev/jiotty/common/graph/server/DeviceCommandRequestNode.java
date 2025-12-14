package net.yudichev.jiotty.common.graph.server;

import jakarta.annotation.Nullable;

public interface DeviceCommandRequestNode<R> extends ServerNode {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        // more readable
    boolean requestPending();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // more readable
    @Nullable
    DeviceRequest<R> currentRequest();
}
