package com.ytdlpjava.task;

import java.nio.file.Path;

public interface TaskResultHandler {
    /**
     * @param title Human-readable title or basename
     * @param resultFile Path to the result file (could be temporary)
     */
    void handle(String title, Path resultFile) throws Exception;
}
