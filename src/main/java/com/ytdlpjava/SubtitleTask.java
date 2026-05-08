package com.ytdlpjava;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
public class SubtitleTask implements VideoTask {
    private final Downloader downloader;
    private final ContentProcessor processor;
    private final FilenameProvider filenameProvider;

    @Override
    public void execute(String url, Path outputDir) throws Exception {
        String title = downloader.getTitle(url);
        String basename = filenameProvider.buildFilename(title, 60);
        
        Path downloadedFile = downloader.download(url, basename);
        try {
            String result = processor.process(downloadedFile);
            
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            
            Path outputPath = outputDir.resolve(basename + ".md");
            Files.writeString(outputPath, result);
            log.info("✅ Success! Subtitles saved to: {}", outputPath);
        } finally {
            if (Files.exists(downloadedFile)) {
                Files.delete(downloadedFile);
                log.debug("Deleted temporary file: {}", downloadedFile);
            }
        }
    }

    @Override
    public FilenameProvider getFilenameProvider() {
        return filenameProvider;
    }
}
