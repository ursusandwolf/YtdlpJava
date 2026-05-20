package com.ytdlpjava.task;

import com.ytdlpjava.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AudioTask implements VideoTask {
    private final MediaDownloader downloader;
    private final TitleProvider titleProvider;
    private final FilenameProvider filenameProvider;
    private final TemporaryFileManager temporaryFileManager;
    private final List<TaskResultHandler> resultHandlers;

    @Override
    public void execute(String url, Path outputDir) throws Exception {
        String title = titleProvider.getTitle(url);
        String basename = filenameProvider.buildFilename(title, 60);
        log.info("Downloading audio for: {}", title);
        Path downloadedFile = downloader.download(url, basename);
        
        try {
            for (TaskResultHandler handler : resultHandlers) {
                handler.handle(title, downloadedFile);
            }
        } finally {
            temporaryFileManager.deleteIfExists(downloadedFile);
        }
    }

    @Override
    public FilenameProvider getFilenameProvider() {
        return filenameProvider;
    }
}
