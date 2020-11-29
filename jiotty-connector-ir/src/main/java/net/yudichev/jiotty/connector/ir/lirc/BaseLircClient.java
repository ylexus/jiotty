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

import com.google.common.collect.ImmutableList;
import net.yudichev.jiotty.common.inject.BaseLifecycleComponent;
import net.yudichev.jiotty.common.lang.Closeable;
import net.yudichev.jiotty.common.lang.PackagePrivateImmutablesStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Collections.emptyList;
import static net.yudichev.jiotty.common.lang.MoreThrowables.asUnchecked;
import static net.yudichev.jiotty.common.lang.MoreThrowables.getAsUnchecked;
import static org.immutables.value.Value.Immutable;
import static org.immutables.value.Value.Parameter;

/**
 * Abstract class for a <a href="http://www.lirc.org">LIRC</a> client.
 * Functionally, it resembles the command line program irsend.
 */
abstract class BaseLircClient extends BaseLifecycleComponent implements LircClient {
    public static final Charset CHARSET = StandardCharsets.US_ASCII;
    private static final Pattern COMMAND_LIST_CLEANUP_PATTERN = Pattern.compile("\\S*\\s+");

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Nullable
    private String lastRemote;
    @Nullable
    private String lastCommand;
    private Streams streams;

    protected BaseLircClient() {
    }

    @Override
    protected final void doStart() {
        streams = connect();
    }

    protected abstract Streams connect();

    @Override
    protected void doStop() {
        streams.close();
    }

    @Override
    public void sendIrCommand(String remote, String command, int count) {
        lastRemote = remote;
        lastCommand = command;
        sendCommand("SEND_ONCE " + remote + " " + command + " " + (count - 1));
    }

    @Override
    public void sendIrCommandRepeat(String remote, String command) {
        lastRemote = remote;
        lastCommand = command;
        sendCommand("SEND_START " + remote + " " + command);
    }

    @Override
    public void stopIr(String remote, String command) {
        sendCommand("SEND_STOP " + remote + " " + command);
    }

    @Override
    public void stopIr() {
        sendCommand("SEND_STOP " + lastRemote + " " + lastCommand);
    }

    @Override
    public List<String> getRemotes() {
        return sendCommand("LIST");
    }

    @Override
    public List<String> getCommands(String remote) {
        return sendCommand("LIST " + checkNotNull(remote)).stream()
                .map(s -> COMMAND_LIST_CLEANUP_PATTERN.matcher(s).replaceAll(""))
                .collect(toImmutableList());
    }

    @Override
    public void setTransmitters(Iterable<Integer> transmitters) {
        long mask = 0L;
        for (int transmitter : transmitters) {
            mask |= (1L << (transmitter - 1));
        }

        setTransmitters(mask);
    }

    @Override
    public void setTransmitters(long mask) {
        sendCommand("SET_TRANSMITTERS " + mask);
    }

    @Override
    public String getVersion() {
        List<String> result = sendCommand("VERSION");
        if (result.isEmpty()) {
            throw new LircServerException("VERSION command returned no lines");
        }
        return result.get(0);
    }

    @Override
    public void setInputLog() {
        setInputLog("null");
    }

    @Override
    public void setInputLog(String logPath) {
        sendCommand("SET_INPUTLOG " + checkNotNull(logPath));
    }

    @Override
    public void setDriverOption(String key, String value) {
        sendCommand("DRV_OPTION " + key + " " + value);
    }

    @Override
    public void simulate(String eventString) {
        sendCommand("SIMULATE " + eventString);
    }

    protected abstract String socketName();

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "NestedSwitchStatement"}) // reads well
    private List<String> sendCommand(String command) {
        logger.debug("Sending command `{}' to Lirc@{}", command, socketName());

        streams.write(command + '\n');

        ImmutableList.Builder<String> resultBuilder = null;
        State state = State.BEGIN;
        int linesReceived = 0;
        int linesExpected = -1;

        while (state != State.DONE) {
            String line = streams.read();
            logger.debug("Received \"{}\"", line);

            if (line == null) {
                state = State.DONE;
                continue;
            }
            switch (state) {
                case BEGIN:
                    if ("BEGIN".equals(line)) {
                        state = State.MESSAGE;
                    }
                    break;
                case MESSAGE:
                    state = line.trim().equalsIgnoreCase(command) ? State.STATUS : State.BEGIN;
                    break;
                case STATUS:
                    switch (line) {
                        case "SUCCESS":
                            state = State.DATA;
                            break;
                        case "END":
                            state = State.DONE;
                            break;
                        case "ERROR":
                            throw new LircServerException("command failed: " + command);
                        default:
                            throw new BadPacketException("unknown response: " + command);
                    }
                    break;
                case DATA:
                    switch (line) {
                        case "END":
                            state = State.DONE;
                            break;
                        case "DATA":
                            state = State.N;
                            break;
                        default:
                            throw new BadPacketException("unknown response: " + command);
                    }
                    break;
                case N:
                    try {
                        linesExpected = Integer.parseInt(line);
                    } catch (NumberFormatException ignored) {
                        throw new BadPacketException("integer expected; got: " + command);
                    }
                    state = linesExpected == 0 ? State.END : State.DATA_N;
                    break;
                case DATA_N:
                    if (resultBuilder == null) {
                        resultBuilder = ImmutableList.builderWithExpectedSize(8);
                    }
                    resultBuilder.add(line);
                    linesReceived++;
                    if (linesReceived == linesExpected) {
                        state = State.END;
                    }
                    break;
                case END:
                    if ("END".equals(line)) {
                        state = State.DONE;
                    } else {
                        throw new BadPacketException("\"END\" expected but \"" + line + "\" received");
                    }
                    break;
                case DONE:
                    break;
                default:
                    throw new RuntimeException("Unhandled case (programming error)");
            }
        }
        logger.debug("Lirc command succeeded.");
        return resultBuilder == null ? emptyList() : resultBuilder.build();
    }

    private enum State {
        BEGIN,
        MESSAGE,
        STATUS,
        DATA,
        N,
        DATA_N,
        END,
        DONE // new
    }

    @Immutable
    @PackagePrivateImmutablesStyle
    abstract static class BaseStreams implements Closeable {
        @Parameter
        public abstract BufferedReader in();

        @Parameter
        public abstract BufferedWriter out();

        void write(String cmd) {
            asUnchecked(() -> {
                out().write(cmd);
                out().flush(); // just to be safe
            });
        }

        String read() {
            return getAsUnchecked(in()::readLine);
        }

        @Override
        public final void close() {
            Closeable.forCloseables(out(), in()).close();
        }
    }

    private static class BadPacketException extends RuntimeException {
        BadPacketException(String message) {
            super(message);
        }
    }
}
