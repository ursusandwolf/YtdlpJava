package com.ytdlpjava;

import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Slf4j
public abstract class AbstractYoutubeService implements Downloader {

    @Override
    public String getTitle(String videoUrl) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--get-title", videoUrl);
        return runCommand(command, "Failed to fetch video title").trim();
    }

    public String getStreamUrl(String videoUrl) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "-f", "bestvideo[ext=mp4]/best", "-g", videoUrl);
        return runCommand(command, "Failed to fetch stream URL").trim();
    }

    public long getDuration(String videoUrl) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--get-duration", "--print", "duration", videoUrl);
        String output = runCommand(command, "Failed to fetch video duration").trim();
        try {
            return (long) Double.parseDouble(output);
        } catch (NumberFormatException e) {
            log.warn("Could not parse duration '{}', defaulting to 0", output);
            return 0;
        }
    }

    protected String runCommand(List<String> command, String errorMessage) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.trace("yt-dlp: {}", line);
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("Command failed: {}. Output: {}", command, output);
            throw new RuntimeException(errorMessage + " (Exit code: " + exitCode + ")");
        }
        return output.toString();
    }
}
