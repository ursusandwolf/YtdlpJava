package com.ytdlpjava.core;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Checks that required external binaries are available before downloads start. */
@Slf4j
public final class ExternalToolsPreflight implements ExternalToolsCheck {
    private static final long PROBE_TIMEOUT_SECONDS = 30;
    private static final Set<String> FFMPEG_TASKS = Set.of("audio", "video", "screenshot");

    private final ProcessExecutor executor;

    public ExternalToolsPreflight(ProcessExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void verify(String taskType) {
        String type = taskType == null ? "" : taskType.trim().toLowerCase(Locale.ROOT);
        require("yt-dlp", List.of("yt-dlp", "--version"));
        require("node", List.of("node", "--version"));
        if (FFMPEG_TASKS.contains(type)) {
            require("ffmpeg", List.of("ffmpeg", "-version"));
        }
        log.info("External tools ready for task '{}'", type);
    }

    private void require(String tool, List<String> command) {
        try {
            String output = executor.run(command, tool + " is required but not available in PATH",
                    PROBE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            String firstLine = output.lines().findFirst().orElse(tool).trim();
            log.info("Found {}: {}", tool, firstLine);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("Interrupted while checking " + tool, e);
        } catch (IOException | RuntimeException e) {
            throw new IllegalArgumentException(
                    tool + " is required but was not found or failed to start. "
                            + "Install it and ensure it is available in PATH. "
                            + "Details: " + e.getMessage(),
                    e);
        }
    }
}
