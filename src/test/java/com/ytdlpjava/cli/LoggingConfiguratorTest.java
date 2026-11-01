package com.ytdlpjava.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.ytdlpjava.config.AppConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingConfiguratorTest {
    private final Logger root = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(Logger.ROOT_LOGGER_NAME);
    private final Level original = root.getLevel();

    @AfterEach
    void restore() {
        root.setLevel(original);
    }

    @Test
    void verboseForcesDebug() {
        AppConfig config = new AppConfig();
        config.setVerbose(true);
        config.setLogLevel("WARN");
        LoggingConfigurator.apply(config);
        assertEquals(Level.DEBUG, root.getLevel());
    }

    @Test
    void logLevelSetsRoot() {
        AppConfig config = new AppConfig();
        config.setLogLevel("WARN");
        LoggingConfigurator.apply(config);
        assertEquals(Level.WARN, root.getLevel());
    }
}
