package com.ytdlpjava;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
        long duration = downloader.getDuration(url);
        String streamUrl = downloader.getStreamUrl(url);
        String basename = filenameProvider.buildFilename(title, 80);

        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        log.info("Capturing screenshots for '{}' (Duration: {}s, Interval: {}s)", title, duration, intervalSeconds);

        for (long ts = 0; ts < duration; ts += intervalSeconds) {
            String timestampStr = formatTimestamp(ts);
            Path outputPath = outputDir.resolve(String.format("%s_%05d.jpg", basename, ts));
            
            log.debug("Capturing frame at {} -> {}", timestampStr, outputPath.getFileName());
            
            List<String> command = List.of(
                "ffmpeg",
                "-ss", String.valueOf(ts),
                "-i", streamUrl,
                "-frames:v", "1",
                "-q:v", "2",
                "-y",
                outputPath.toString()
            );

            runFfmpeg(command);
        }
        
        log.info("✅ Screenshot capture complete.");
    }

    private void runFfmpeg(List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.trace("ffmpeg: {}", line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("ffmpeg failed for command: {}", command);
            throw new RuntimeException("ffmpeg extraction failed with exit code " + exitCode);
        }
    }

    private String formatTimestamp(long seconds) {
        return String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }
}
