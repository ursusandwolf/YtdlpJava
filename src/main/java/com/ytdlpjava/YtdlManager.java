package com.ytdlpjava;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
public class YtdlManager {
    private final VideoTask task;

    public void process(String url, Path outputDir) {
        try {
            task.execute(url, outputDir);
        } catch (Exception e) {
            log.error("Failed to process video: {}", e.getMessage());
            // We don't call System.exit here, let the caller decide
            throw new RuntimeException(e);
        }
    }
}
