package net.yudichev.jiotty.common.rest;

import spark.Route;

public interface RestServer {
    void post(String url, Route route);

    void get(String path, Route route);
}
