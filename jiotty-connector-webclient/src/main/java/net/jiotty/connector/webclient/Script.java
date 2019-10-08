package net.jiotty.connector.webclient;

import com.gargoylesoftware.htmlunit.WebClient;

public interface Script<T> {
    @SuppressWarnings("ProhibitedExceptionDeclared")
        // by design - WebClient throws checked exceptions
    T execute(WebClient webClient) throws Exception;
}
