package net.yudichev.jiotty.connector.ir.lirc;

import java.util.List;

public interface LircClient {
    default void sendIrCommand(String remote, String command) {
        sendIrCommand(remote, command, 1);
    }

    void sendIrCommand(String remote, String command, int count);

    void sendIrCommandRepeat(String remote, String command);

    void stopIr(String remote, String command);

    void stopIr();

    List<String> getRemotes();

    List<String> getCommands(String remote);

    void setTransmitters(Iterable<Integer> transmitters);

    void setTransmitters(long mask);

    String getVersion();

    void setInputLog();

    void setInputLog(String logPath);

    void setDriverOption(String key, String value);

    void simulate(String eventString);
}
