package com.ytdlpjava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class YoutubeDownloader {
    public Path downloadSubtitles(String videoUrl, String lang, String outputBasename) throws IOException, InterruptedException {
        Path subtitlePath = Path.of(outputBasename + "." + lang + ".vtt");

        List<String> command = new ArrayList<>(List.of(
                "yt-dlp",
                "--write-auto-sub",
                "--sub-lang", lang,
                "--skip-download",
                "--output", outputBasename,
                videoUrl
        ));

        runCommand(command, "yt-dlp failed");

        if (!Files.exists(subtitlePath)) {
            throw new IOException("Subtitle file not found: " + subtitlePath);
        }

        return subtitlePath;
    }

    public String getVideoTitle(String videoUrl) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--print", "title", videoUrl);
        return runCommand(command, "Failed to fetch video title").trim();
    }

    private String runCommand(List<String> command, String errorMessage) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) output.append(line).append("\n");
        }
        if (process.waitFor() != 0) throw new RuntimeException(errorMessage + ": " + output);
        return output.toString();
    }
}
