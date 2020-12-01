/*
Copyright (C) 2016, 2017 Bengt Martensson.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see http://www.gnu.org/licenses/.
*/
package net.yudichev.jiotty.connector.ir.lirc;

import com.google.inject.BindingAnnotation;
import net.yudichev.jiotty.common.async.ExecutorFactory;
import net.yudichev.jiotty.common.async.backoff.RetryableOperationExecutor;
import net.yudichev.jiotty.common.lang.Closeable;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.toIntExact;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;

/**
 * An implementation of the LircClient using an TCP port, per default localhost at port 8765.
 */
final class TcpLircClient extends BaseLircClient {
    public static final String DEFAULTLIRCIP = "127.0.0.1"; // localhost
    private final int port;
    private Socket socket;
    private final InetAddress inetAddress;
    private final Duration timeout;
    private final String connectionName;

    @Inject
    TcpLircClient(ExecutorFactory executorFactory,
                  @Address String address,
                  @Port int port,
                  @Timeout Duration timeout,
                  @Dependency RetryableOperationExecutor retryableOperationExecutor) {
        super(executorFactory, retryableOperationExecutor);
        String lircServerIp = (address != null) ? address : DEFAULTLIRCIP;
        this.port = port;
        inetAddress = getAsUnchecked(() -> InetAddress.getByName(lircServerIp));
        this.timeout = checkNotNull(timeout);
        connectionName = inetAddress.getCanonicalHostName() + ':' + port;
    }

    @Override
    protected Streams connect() {
        logger.debug("Connecting socket to {} with timeout {}", connectionName(), timeout);

        return getAsUnchecked(() -> {
            if (socket != null) {
                socket.close();
            }
            socket = new Socket();
            int timeoutMillis = toIntExact(timeout.toMillis());
            socket.connect(new InetSocketAddress(inetAddress, port), timeoutMillis);
            socket.setSoTimeout(timeoutMillis);
            socket.setKeepAlive(true);
            BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), CHARSET));
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream(), CHARSET));
            return Streams.of(inFromServer, outToServer);
        });
    }

    @Override
    protected String connectionName() {
        return connectionName;
    }

    @Override
    protected void doStop() {
        super.doStop();
        Closeable.closeSafelyIfNotNull(logger, socket);
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Address {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Port {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Timeout {
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }
}
