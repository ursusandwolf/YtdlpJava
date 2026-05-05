package com.ytdlpjava;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractYoutubeService implements Downloader {
    protected final ProcessExecutor executor;

    @Override
    public String getTitle(String videoUrl) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--no-warnings", "--print", "title", videoUrl);
        return executor.run(command, "Failed to fetch video title");
    }

    public String getStreamUrl(String videoUrl) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--no-warnings", "-f", "bestvideo[ext=mp4]/best[ext=mp4]/best", "-g", videoUrl);
        return executor.run(command, "Failed to fetch stream URL");
    }

    public long getDuration(String videoUrl) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--no-warnings", "--print", "%(duration)j", videoUrl);
        String output = executor.run(command, "Failed to fetch video duration");
        try {
            return Long.parseLong(output);
        } catch (NumberFormatException e) {
            log.warn("Could not parse duration '{}', trying fallback", output);
            List<String> fallbackCmd = List.of("yt-dlp", "--no-warnings", "--print", "duration", videoUrl);
            String fallbackOutput = executor.run(fallbackCmd, "Failed to fetch duration fallback");
            try {
                return (long) Double.parseDouble(fallbackOutput);
            } catch (NumberFormatException e2) {
                log.error("Total failure parsing duration: {}", fallbackOutput);
                return 0;
            }
        }
    }

    public List<String> getPlaylistUrls(String url) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--no-warnings", "--flat-playlist", "--print", "webpage_url", url);
        String output = executor.run(command, "Failed to fetch playlist URLs");
        
        List<String> urls = output.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.equals("NA"))
                .toList();
        
        if (urls.isEmpty()) return List.of(url);
        return urls;
    }
}
