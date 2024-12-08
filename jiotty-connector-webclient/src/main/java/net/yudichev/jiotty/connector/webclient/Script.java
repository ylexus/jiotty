package net.yudichev.jiotty.connector.webclient;

import org.htmlunit.WebClient;

public interface Script<T> {
    @SuppressWarnings("ProhibitedExceptionDeclared")
        // by design - WebClient throws checked exceptions
    T execute(WebClient webClient) throws Exception;
}
