package com.ytdlpjava;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class YtdlManager {
    private final VideoTask task;
    private final Downloader downloader;

    public void process(String url, Path outputDir) {
        try {
            List<String> urls = downloader.getPlaylistUrls(url);
            log.info("Found {} item(s) to process", urls.size());
            
            for (int i = 0; i < urls.size(); i++) {
                String currentUrl = urls.get(i);
                log.info("Processing [{}/{}]: {}", i + 1, urls.size(), currentUrl);
                try {
                    task.execute(currentUrl, outputDir);
                } catch (Exception e) {
                    log.error("Failed to process item {}: {}", currentUrl, e.getMessage());
                    // Continue with next item in playlist instead of failing entirely
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch playlist or process: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
