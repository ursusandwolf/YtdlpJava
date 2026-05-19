package com.ytdlpjava.task;

import com.ytdlpjava.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SubtitleTask implements VideoTask {
    private final Downloader downloader;
    private final ContentProcessor processor;
    private final FilenameProvider filenameProvider;
    private final List<TaskResultHandler> resultHandlers;

    @Override
    public void execute(String url, Path outputDir) throws Exception {
        String title = downloader.getTitle(url);
        String basename = filenameProvider.buildFilename(title, 60);
        
        Path downloadedFile = downloader.download(url, basename);
        Path tempSubtitleFile = null;
        try {
            String result = processor.process(downloadedFile);
            
            // Create a temporary .md file
            tempSubtitleFile = Files.createTempFile(basename, ".md");
            Files.writeString(tempSubtitleFile, result);
            
            for (TaskResultHandler handler : resultHandlers) {
                handler.handle(title, tempSubtitleFile);
            }
        } finally {
            if (Files.exists(downloadedFile)) {
                Files.delete(downloadedFile);
                log.debug("Deleted temporary VTT file: {}", downloadedFile);
            }
            if (tempSubtitleFile != null && Files.exists(tempSubtitleFile)) {
                try {
                    Files.delete(tempSubtitleFile);
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public FilenameProvider getFilenameProvider() {
        return filenameProvider;
    }
}
