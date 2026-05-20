package com.ytdlpjava.task;

import com.ytdlpjava.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class MetadataTask implements VideoTask {
    private final MediaDownloader downloader;
    private final TitleProvider titleProvider;
    private final FilenameProvider filenameProvider;
    private final TemporaryFileManager temporaryFileManager;
    private final List<TaskResultHandler> resultHandlers;

    @Override
    public void execute(String url, Path outputDir) throws Exception {
        String title = titleProvider.getTitle(url);
        String basename = filenameProvider.buildFilename(title, 60);
        
        log.info("Starting metadata extraction for: {}", title);
        Path result = downloader.download(url, basename);
        
        try {
            for (TaskResultHandler handler : resultHandlers) {
                handler.handle(title, result);
            }
        } finally {
            temporaryFileManager.deleteIfExists(result);
        }
    }

    @Override
    public FilenameProvider getFilenameProvider() {
        return filenameProvider;
    }
}
