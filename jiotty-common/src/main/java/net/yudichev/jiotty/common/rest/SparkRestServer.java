package net.yudichev.jiotty.common.rest;

import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;
import spark.Service;

final class SparkRestServer extends BaseLifecycleComponent implements RestServer {
    private static final Logger logger = LoggerFactory.getLogger(SparkRestServer.class);
    private volatile Service http;

    @Override
    public void doStart() {
        http = Service.ignite().port(4567);
        http.initExceptionHandler(e -> logger.error("Spark service failed", e));
        logger.info("Spark service ignited on port 4567: {}", http);
    }

    @Override
    public void post(String url, Route route) {
        checkStarted();
        http.post(url, route);
    }

    @Override
    public void get(String path, Route route) {
        checkStarted();
        http.get(path, route);
    }

    @Override
    protected void doStop() {
        http.stop();
        http.awaitStop();
    }
}
