package net.jiotty.connector.webclient;

import com.gargoylesoftware.htmlunit.WebClient;

public interface Script<T> {
    T execute(WebClient webClient) throws Exception;
}
