package com.ytdlpjava.task;

import java.nio.file.Path;

public interface TaskResultHandler {
    /**
     * @param title Human-readable title or basename
     * @param resultFile Path to the result file (could be temporary)
     */
    void handle(String title, Path resultFile) throws Exception;

    /** Handlers must leave the source file available for subsequent handlers. */
    default void handle(String title, Path resultFile, Path outputDir) throws Exception {
        handle(title, resultFile);
    }
}
