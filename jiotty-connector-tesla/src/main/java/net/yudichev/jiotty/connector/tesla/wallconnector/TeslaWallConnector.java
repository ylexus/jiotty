package net.yudichev.jiotty.connector.tesla.wallconnector;

import java.util.concurrent.CompletableFuture;

public interface TeslaWallConnector {
    CompletableFuture<TeslaWallConnectorVitals> getVitals();
}
