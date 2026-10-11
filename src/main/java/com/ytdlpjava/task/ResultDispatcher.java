package com.ytdlpjava.task;

import java.nio.file.Path;
import java.util.List;

/** Result files remain owned by the task until every handler has been attempted. */
public final class ResultDispatcher {
    private ResultDispatcher() {}

    public static void dispatch(List<TaskResultHandler> handlers, String title, Path resultFile, Path outputDir)
            throws Exception {
        Exception failure = null;
        for (TaskResultHandler handler : handlers) {
            try {
                handler.handle(title, resultFile, outputDir);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            } catch (Exception e) {
                if (failure == null) {
                    failure = new Exception("One or more result handlers failed", e);
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        if (failure != null) throw failure;
    }
}
