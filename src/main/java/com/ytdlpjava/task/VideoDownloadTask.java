package com.ytdlpjava.task;

import com.ytdlpjava.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class VideoDownloadTask implements VideoTask {
    private final Downloader downloader;
    private final FilenameProvider filenameProvider;
    private final List<TaskResultHandler> resultHandlers;

    @Override
    public void execute(String url, Path outputDir) throws Exception {
        String title = downloader.getTitle(url);
        String basename = filenameProvider.buildFilename(title, 60);
        
        log.info("Downloading video: {}", title);
        Path downloadedFile = downloader.download(url, basename);

        try {
            for (TaskResultHandler handler : resultHandlers) {
                handler.handle(title, downloadedFile);
            }
        } finally {
            if (Files.exists(downloadedFile)) {
                try {
                    Files.delete(downloadedFile);
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public FilenameProvider getFilenameProvider() {
        return filenameProvider;
    }
}
