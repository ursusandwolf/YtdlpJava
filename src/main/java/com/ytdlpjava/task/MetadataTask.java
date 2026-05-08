package com.ytdlpjava.task;

import com.ytdlpjava.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
public class MetadataTask implements VideoTask {
    private final Downloader downloader;
    private final FilenameProvider filenameProvider;

    @Override
    public void execute(String url, Path outputDir) throws Exception {
        String title = downloader.getTitle(url);
        String basename = filenameProvider.buildFilename(title, 60);
        
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
            log.debug("Created output directory: {}", outputDir);
        }
        
        log.info("Starting metadata extraction for: {}", title);
        Path result = downloader.download(url, outputDir.resolve(basename).toString());
        log.info("✅ Metadata successfully saved to: {}", result);
    }

    @Override
    public FilenameProvider getFilenameProvider() {
        return filenameProvider;
    }
}
