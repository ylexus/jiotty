package net.yudichev.jiotty.logging;

import java.util.Map;

public interface LoggingLevelConfigurator {
    void setLoggingLevel(String loggerName, String logLevel);

    void setLoggingLevels(Map<String, String> levelsByLoggerName);

    void resetLoggingLevel(String loggerName);

    /// @return logging levels that were set via this service
    Map<String, String> getLevelsByLoggerName();
}
