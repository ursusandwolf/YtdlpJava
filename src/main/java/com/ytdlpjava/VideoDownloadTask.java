package com.ytdlpjava;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@RequiredArgsConstructor
public class VideoDownloadTask implements VideoTask {
    private final Downloader downloader;
    private final FilenameProvider filenameProvider;

    @Override
    public void execute(String url, Path outputDir) throws Exception {
        String title = downloader.getTitle(url);
        String basename = filenameProvider.buildFilename(title, 80);
        
        log.info("Downloading video: {}", title);
        Path downloadedFile = downloader.download(url, basename);

        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        Path targetPath = outputDir.resolve(downloadedFile.getFileName());
        Files.move(downloadedFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("✅ Video successfully saved to: {}", targetPath);
    }
}
