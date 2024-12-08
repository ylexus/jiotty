package net.yudichev.jiotty.common.rest;

import io.javalin.Javalin;
import io.javalin.http.Handler;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JavalinRestServer extends BaseLifecycleComponent implements RestServer {
    private static final Logger logger = LoggerFactory.getLogger(JavalinRestServer.class);
    private volatile Javalin javalin;

    @Override
    public void doStart() {
        javalin = Javalin.create().start(4567);
        logger.info("REST service started on port 4567: {}", javalin);
    }

    @Override
    public void post(String url, Handler handler) {
        checkStarted();
        javalin.post(url, handler);
    }

    @Override
    public void get(String path, Handler handler) {
        checkStarted();
        javalin.get(path, handler);
    }

    @Override
    protected void doStop() {
        javalin.stop();
    }
}
