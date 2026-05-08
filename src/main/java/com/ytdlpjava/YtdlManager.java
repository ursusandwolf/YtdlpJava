package com.ytdlpjava;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
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

            Path effectiveOutputDir = outputDir;
            if (urls.size() > 1 || url.contains("playlist?list=")) {
                String playlistTitle = downloader.getPlaylistTitle(url);
                if (playlistTitle != null && !playlistTitle.isEmpty() && !"NA".equalsIgnoreCase(playlistTitle)) {
                    String sanitizedTitle = task.getFilenameProvider().buildFilename(playlistTitle, 60);
                    effectiveOutputDir = outputDir.resolve(sanitizedTitle);
                    if (!Files.exists(effectiveOutputDir)) {
                        Files.createDirectories(effectiveOutputDir);
                        log.info("Created playlist directory: {}", effectiveOutputDir);
                    }
                }
            }
            
            for (int i = 0; i < urls.size(); i++) {
                String currentUrl = urls.get(i);
                log.info("Processing [{}/{}]: {}", i + 1, urls.size(), currentUrl);
                try {
                    task.execute(currentUrl, effectiveOutputDir);
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
