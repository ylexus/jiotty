package net.yudichev.jiotty.logging;

import net.yudichev.jiotty.common.varstore.InMemoryVarStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"StaticVariableUsedBeforeInitialization", "StaticVariableMayNotBeInitialized"}) // static @BeforeAll mechanism used
@Isolated
class PersistingLog4jLevelConfiguratorTest {
    private static final String LOGGER_NAME = PersistingLog4jLevelConfiguratorTest.class.getSimpleName() + "_logger";
    private static Path logFile;
    private static InMemoryVarStore varStore;
    private static Logger logger;
    private static PersistingLog4jLevelConfigurator configurator;
    private static int verificationCounter;

    @BeforeAll
    static void setUp() throws IOException {
        logFile = Files.createTempFile(PersistingLog4jLevelConfiguratorTest.class.getSimpleName(), ".log");
        System.setProperty("test.file.appender.path", logFile.toString());
        logger = LogManager.getLogger(LOGGER_NAME);

        varStore = new InMemoryVarStore();

        startConfigurator();
    }

    @AfterAll
    static void tearDown() throws IOException {
        System.getProperties().remove("test.file.appender.path");
        Files.delete(logFile);
    }

    @Test
    void changesLoggingLevel() throws IOException {
        configurator.setLoggingLevel(LOGGER_NAME, "TRACE");
        verifyTraceLevel();
        restartConfigurator();
        verifyTraceLevel();

        configurator.setLoggingLevels(Map.of(LOGGER_NAME, "DEBUG"));
        verifyDebugLevel();
        restartConfigurator();
        verifyDebugLevel();

        assertThat(configurator.getLevelsByLoggerName()).containsOnlyKeys(LOGGER_NAME).containsValue("DEBUG");

        configurator.resetLoggingLevel(LOGGER_NAME);
        verifyInfoLevel();
        restartConfigurator();
        verifyInfoLevel();
    }

    private static void restartConfigurator() {
        configurator.stop();
        startConfigurator();
    }

    private static void startConfigurator() {
        configurator = new PersistingLog4jLevelConfigurator(varStore, "prefix");
        configurator.start();
    }

    private static void verifyInfoLevel() throws IOException {
        verify("infoMsg");
    }

    private static void verifyDebugLevel() throws IOException {
        verify("debugMsg", "infoMsg");
    }

    private static void verifyTraceLevel() throws IOException {
        verify("traceMsg", "debugMsg", "infoMsg");
    }

    private static void verify(String... expected) throws IOException {
        var verificationId = verificationCounter++;
        var vidMarker = "VID=" + verificationId;
        logger.trace("traceMsg{}", vidMarker);
        logger.debug("debugMsg{}", vidMarker);
        logger.info("infoMsg{}", vidMarker);
        assertThat(Files.readAllLines(logFile).stream()
                        .filter(s -> s.startsWith(LOGGER_NAME))
                        .filter(s -> s.contains(vidMarker))
                        .map(s -> s.substring(LOGGER_NAME.length() + 1, s.length() - vidMarker.length())))
                .containsExactly(expected);
    }
}