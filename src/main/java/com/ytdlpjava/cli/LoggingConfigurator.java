package com.ytdlpjava.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.ytdlpjava.config.AppConfig;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/** Applies CLI log level overrides on top of logback.xml defaults. */
public final class LoggingConfigurator {
    private LoggingConfigurator() {}

    public static void apply(AppConfig config) {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext context)) {
            return;
        }
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        if (config.isVerbose()) {
            root.setLevel(Level.DEBUG);
            return;
        }
        if (config.getLogLevel() != null && !config.getLogLevel().isBlank()) {
            root.setLevel(Level.valueOf(config.getLogLevel().trim().toUpperCase(Locale.ROOT)));
        }
    }
}
