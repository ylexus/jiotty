/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * <p>Copy from a google library patched to avoid waiting uninterruptibly for the callback, as it is not
 * compatible with the concept of interrupting Application startup.
 */

package net.yudichev.jiotty.connector.google.common;

import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.util.Throwables;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

/**
 * OAuth 2.0 verification code receiver that runs a Jetty server on a free port, waiting for a
 * redirect with the verification code.
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * @author Yaniv Inbar
 * @since 1.11
 */
public final class LocalServerReceiver implements VerificationCodeReceiver {

    private static final String LOCALHOST = "localhost";

    private static final String CALLBACK_PATH = "/Callback";
    /**
     * To block until receiving an authorization response or stop() is called.
     */
    private final Semaphore waitUnlessSignaled = new Semaphore(0 /* initially zero permit */);
    /**
     * Host name to use.
     */
    private final String host;
    /**
     * Callback path of redirect_uri
     */
    private final String callbackPath;
    /**
     * URL to an HTML page to be shown (via redirect) after successful login. If null, a canned
     * default landing page will be shown (via direct response).
     */
    private final String successLandingPageUrl;
    /**
     * URL to an HTML page to be shown (via redirect) after failed login. If null, a canned
     * default landing page will be shown (via direct response).
     */
    private final String failureLandingPageUrl;
    /**
     * Server or {@code null} before {@link #getRedirectUri()}.
     */
    @Nullable
    private Server server;
    /**
     * Verification code or {@code null} for none.
     */
    private String code;
    /**
     * Error code or {@code null} for none.
     */
    private String error;
    /**
     * Port to use or {@code -1} to select an unused port in {@link #getRedirectUri()}.
     */
    private int port;

    /**
     * Constructor.
     *
     * @param host Host name to use
     * @param port Port to use or {@code -1} to select an unused port
     */
    LocalServerReceiver(String host, int port,
                        String successLandingPageUrl, String failureLandingPageUrl) {
        this(host, port, CALLBACK_PATH, successLandingPageUrl, failureLandingPageUrl);
    }

    /**
     * Constructor.
     *
     * @param host Host name to use
     * @param port Port to use or {@code -1} to select an unused port
     */
    LocalServerReceiver(String host, int port, String callbackPath,
                        String successLandingPageUrl, String failureLandingPageUrl) {
        this.host = host;
        this.port = port;
        this.callbackPath = callbackPath;
        this.successLandingPageUrl = successLandingPageUrl;
        this.failureLandingPageUrl = failureLandingPageUrl;
    }

    @Override
    public String getRedirectUri() throws IOException {
        server = new Server((ThreadPool) null);
        var connector = new ServerConnector(server);
        connector.setHost(host);
        connector.setPort(port == -1 ? 0 : port);
        server.addConnector(connector);
        server.setHandler(new CallbackHandler());
        try {
            server.start();
            port = connector.getLocalPort();
        } catch (Exception e) {
            Throwables.propagateIfPossible(e);
            throw new IOException(e);
        }
        return "http://" + connector.getHost() + ":" + port + callbackPath;
    }

    /**
     * Blocks until the server receives a login result, or the server is stopped
     * by {@link #stop()}, to return an authorization code.
     *
     * @return authorization code if login succeeds; may return {@code null} if the server
     * is stopped by {@link #stop()}
     * @throws IOException if the server receives an error code (through an HTTP request
     *                     parameter {@code error})
     */
    @Override
    public String waitForCode() throws IOException {
        try {
            waitUnlessSignaled.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        if (error != null) {
            throw new IOException("User authorization failed (" + error + ")");
        }
        return code;
    }

    @Override
    public void stop() throws IOException {
        waitUnlessSignaled.release();
        if (server != null) {
            // must temporary clear the Thread interrupted flag, else jetty won't be able to stop its threads
            var wasInterrupted = Thread.interrupted();
            try {
                server.stop();
            } catch (Exception e) {
                Throwables.propagateIfPossible(e);
                throw new IOException(e);
            } finally {
                if (wasInterrupted) {
                    Thread.currentThread().interrupt();
                }
            }
            server = null;
        }
    }

    /**
     * Builder.
     *
     * <p>
     * Implementation is not thread-safe.
     * </p>
     */
    public static final class Builder {

        /**
         * Host name to use.
         */
        private String host = LOCALHOST;

        /**
         * Port to use or {@code -1} to select an unused port.
         */
        private int port = -1;

        private String successLandingPageUrl;
        private String failureLandingPageUrl;

        private String callbackPath = CALLBACK_PATH;

        public LocalServerReceiver build() {
            return new LocalServerReceiver(host, port, callbackPath,
                                           successLandingPageUrl, failureLandingPageUrl);
        }

        public String getHost() {
            return host;
        }

        /**
         * Sets the host name to use.
         */
        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public int getPort() {
            return port;
        }

        /**
         * Sets the port to use or {@code -1} to select an unused port.
         */
        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public String getCallbackPath() {
            return callbackPath;
        }

        /**
         * Set the callback path of redirect_uri
         */
        public Builder setCallbackPath(String callbackPath) {
            this.callbackPath = callbackPath;
            return this;
        }

        public Builder setLandingPages(String successLandingPageUrl, String failureLandingPageUrl) {
            this.successLandingPageUrl = successLandingPageUrl;
            this.failureLandingPageUrl = failureLandingPageUrl;
            return this;
        }
    }

    /**
     * Jetty handler that takes the verifier token passed over from the OAuth provider and stashes it
     * where {@link #waitForCode} will find it.
     */
    class CallbackHandler extends AbstractHandler {

        private void writeLandingHtml(HttpServletResponse response) throws IOException {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");

            PrintWriter doc = response.getWriter();
            doc.println("<html>");
            doc.println("<head><title>OAuth 2.0 Authentication Token Received</title></head>");
            doc.println("<body>");
            doc.println("Received verification code. You may now close this window.");
            doc.println("</body>");
            doc.println("</html>");
            doc.flush();
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            if (!callbackPath.equals(target)) {
                return;
            }

            try {
                ((Request) request).setHandled(true);
                error = request.getParameter("error");
                code = request.getParameter("code");

                if (error == null && successLandingPageUrl != null) {
                    response.sendRedirect(successLandingPageUrl);
                } else if (error != null && failureLandingPageUrl != null) {
                    response.sendRedirect(failureLandingPageUrl);
                } else {
                    writeLandingHtml(response);
                }
                response.flushBuffer();
            } finally {
                waitUnlessSignaled.release();
            }
        }
    }
}
