package com.ytdlpjava.task;

import com.ytdlpjava.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@RequiredArgsConstructor
public class AudioTask implements VideoTask {
    private final Downloader downloader;
    private final FilenameProvider filenameProvider;

    @Override
    public void execute(String url, Path outputDir) throws Exception {
        String title = downloader.getTitle(url);
        String basename = filenameProvider.buildFilename(title, 60);
        log.info("Downloading audio for: {}", title);
        Path downloadedFile = downloader.download(url, basename);
        log.debug("Downloaded audio file path: {}", downloadedFile);

        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
            log.debug("Created output directory: {}", outputDir);
        }

        Path targetPath = outputDir.resolve(downloadedFile.getFileName());
        log.debug("Moving {} to {}", downloadedFile, targetPath);
        Files.move(downloadedFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("✅ Audio successfully saved to: {}", targetPath);
    }

    @Override
    public FilenameProvider getFilenameProvider() {
        return filenameProvider;
    }
}
