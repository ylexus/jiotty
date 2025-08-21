package net.yudichev.jiotty.connector.google.maps;

import java.util.concurrent.CompletableFuture;

public interface RoutesService {
    CompletableFuture<Routes> computeRoutes(RouteParameters parameters);
}
