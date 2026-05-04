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
        List<String> command = List.of("yt-dlp", "--no-warnings", "--print", "title", videoUrl);
        return runCommand(command, "Failed to fetch video title").trim();
    }

    public String getStreamUrl(String videoUrl) throws IOException, InterruptedException {
        // Try to get a single file URL (mp4) which is better for random access seeking
        List<String> command = List.of("yt-dlp", "--no-warnings", "-f", "bestvideo[ext=mp4]/best[ext=mp4]/best", "-g", videoUrl);
        return runCommand(command, "Failed to fetch stream URL").trim();
    }

    public long getDuration(String videoUrl) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--no-warnings", "--print", "%(duration)j", videoUrl);
        String output = runCommand(command, "Failed to fetch video duration").trim();
        try {
            return Long.parseLong(output);
        } catch (NumberFormatException e) {
            log.warn("Could not parse duration '{}', trying fallback", output);
            // Fallback: try to get it without JSON format
            List<String> fallbackCmd = List.of("yt-dlp", "--no-warnings", "--print", "duration", videoUrl);
            String fallbackOutput = runCommand(fallbackCmd, "Failed to fetch duration fallback").trim();
            try {
                return (long) Double.parseDouble(fallbackOutput);
            } catch (NumberFormatException e2) {
                log.error("Total failure parsing duration: {}", fallbackOutput);
                return 0;
            }
        }
    }

    public List<String> getPlaylistUrls(String url) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--no-warnings", "--flat-playlist", "--print", "url", url);
        String output = runCommand(command, "Failed to fetch playlist URLs").trim();
        if (output.isEmpty()) return List.of(url);
        return List.of(output.split("\\n"));
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
