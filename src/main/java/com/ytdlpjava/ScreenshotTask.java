package com.ytdlpjava;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ScreenshotTask implements VideoTask {
    private final Downloader downloader;
    private final FilenameProvider filenameProvider;
    private final int intervalSeconds;

    @Override
    public void execute(String url, Path outputDir) throws Exception {
        String title = downloader.getTitle(url);
        String basename = filenameProvider.buildFilename(title, 80);

        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        log.info("Downloading video for reliable screenshot extraction: {}", title);
        Path videoFile = downloader.download(url, basename);
        
        try {
            long duration = downloader.getDuration(url);
            log.info("Capturing screenshots for '{}' (Duration: {}s, Interval: {}s)", title, duration, intervalSeconds);

            ProcessExecutor executor = new ProcessExecutor(); // In a real app, this should be injected
            for (long ts = 0; ts < duration; ts += intervalSeconds) {
                Path outputPath = outputDir.resolve(String.format("%s_%05d.jpg", basename, ts));
                
                log.debug("Capturing frame at {} -> {}", ts, outputPath.getFileName());
                
                List<String> command = List.of(
                    "ffmpeg",
                    "-ss", String.valueOf(ts),
                    "-i", videoFile.toString(),
                    "-frames:v", "1",
                    "-q:v", "2",
                    "-y",
                    outputPath.toString()
                );

                executor.run(command, "ffmpeg extraction failed");
            }
            log.info("✅ Screenshot capture complete.");
        } finally {
            if (Files.exists(videoFile)) {
                Files.delete(videoFile);
                log.debug("Deleted temporary video file: {}", videoFile);
            }
        }
    }
}
