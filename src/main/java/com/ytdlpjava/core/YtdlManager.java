package com.ytdlpjava.core;

import com.ytdlpjava.model.PlaylistProvider;
import com.ytdlpjava.model.VideoTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class YtdlManager {
    private final VideoTask task;
    private final PlaylistProvider playlistProvider;

    public void process(String url, Path outputDir) {
        try {
            List<String> urls = playlistProvider.getPlaylistUrls(url);
            if (urls == null || urls.isEmpty()) {
                throw new YtdlManagerException("No videos found for " + url, null);
            }
            log.info("Found {} item(s) to process", urls.size());
            Path effectiveOutputDir = outputDir;
            if (urls.size() > 1 || url.contains("playlist?list=")) {
                String title = playlistProvider.getPlaylistTitle(url);
                if (title != null && !title.isBlank() && !"NA".equalsIgnoreCase(title)) {
                    String name = task.getFilenameProvider().buildFilename(title, 60);
                    effectiveOutputDir = outputDir.resolve(name);
                }
            }
            Files.createDirectories(effectiveOutputDir);
            List<Exception> failures = new ArrayList<>();
            for (int i = 0; i < urls.size(); i++) {
                String currentUrl = urls.get(i);
                log.info("Processing [{}/{}]: {}", i + 1, urls.size(), currentUrl);
                try {
                    task.execute(currentUrl, effectiveOutputDir);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new YtdlManagerException("Processing interrupted", e);
                } catch (Exception e) {
                    failures.add(e);
                    log.error("Failed to process item {}: {}", currentUrl, e.getMessage());
                }
            }
            log.info("Processing summary: {} successful, {} failed", urls.size() - failures.size(), failures.size());
            if (!failures.isEmpty()) {
                YtdlManagerException failure = new YtdlManagerException(
                        failures.size() + " of " + urls.size() + " item(s) failed for " + url, failures.getFirst());
                failures.stream().skip(1).forEach(failure::addSuppressed);
                throw failure;
            }
        } catch (YtdlManagerException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new YtdlManagerException("Processing interrupted", e);
        } catch (Exception e) {
            throw new YtdlManagerException("Failed to fetch playlist or process: " + e.getMessage(), e);
        }
    }
}
