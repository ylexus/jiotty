package net.yudichev.jiotty.connector.ir.lirc;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LircClient {
    default CompletableFuture<Void> sendIrCommand(String remote, String command) {
        return sendIrCommand(remote, command, 1);
    }

    CompletableFuture<Void> sendIrCommand(String remote, String command, int count);

    CompletableFuture<Void> sendIrCommandRepeat(String remote, String command);

    CompletableFuture<Void> stopIr(String remote, String command);

    CompletableFuture<List<String>> getRemotes();

    CompletableFuture<List<String>> getCommands(String remote);

    CompletableFuture<Void> setTransmitters(Iterable<Integer> transmitters);

    CompletableFuture<Void> setTransmitters(long mask);

    CompletableFuture<String> getVersion();

    CompletableFuture<Void> setInputLog();

    CompletableFuture<Void> setInputLog(String logPath);

    CompletableFuture<Void> setDriverOption(String key, String value);

    CompletableFuture<Void> simulate(String eventString);
}
