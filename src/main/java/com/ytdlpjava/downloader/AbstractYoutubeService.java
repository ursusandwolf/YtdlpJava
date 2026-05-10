package com.ytdlpjava.downloader;

import com.ytdlpjava.core.ProcessExecutor;
import com.ytdlpjava.model.Downloader;import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractYoutubeService implements Downloader {
    protected final ProcessExecutor executor;

    @Override
    public String getTitle(String videoUrl) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--no-warnings", "--ignore-errors", "--print", "title", videoUrl);
        try {
            return executor.run(command, "Failed to fetch video title").trim();
        } catch (Exception e) {
            log.warn("Could not fetch video title for {}. Error: {}", videoUrl, e.getMessage());
            return "Unknown_Video_" + System.currentTimeMillis();
        }
    }

    public String getStreamUrl(String videoUrl) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--no-warnings", "--ignore-errors", "-f", "bestvideo[ext=mp4]/best[ext=mp4]/best", "-g", videoUrl);
        return executor.run(command, "Failed to fetch stream URL");
    }

    public long getDuration(String videoUrl) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--no-warnings", "--ignore-errors", "--print", "%(duration)j", videoUrl);
        try {
            String output = executor.run(command, "Failed to fetch video duration");
            return Long.parseLong(output);
        } catch (Exception e) {
            log.warn("Could not parse duration for {}, trying fallback. Error: {}", videoUrl, e.getMessage());
            List<String> fallbackCmd = List.of("yt-dlp", "--no-warnings", "--ignore-errors", "--print", "duration", videoUrl);
            try {
                String fallbackOutput = executor.run(fallbackCmd, "Failed to fetch duration fallback");
                return (long) Double.parseDouble(fallbackOutput);
            } catch (Exception e2) {
                log.error("Total failure parsing duration for {}: {}", videoUrl, e2.getMessage());
                return 0;
            }
        }
    }

    public List<String> getPlaylistUrls(String url) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--no-warnings", "--ignore-errors", "--flat-playlist", "--print", "webpage_url", url);
        try {
            String output = executor.run(command, "Failed to fetch playlist URLs");
            List<String> urls = output.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.equals("NA"))
                    .toList();

            if (urls.isEmpty()) return List.of(url);
            return urls;
        } catch (Exception e) {
            log.warn("Could not fetch playlist URLs for {}, treating as single video. Error: {}", url, e.getMessage());
            return List.of(url);
        }
    }

    @Override
    public String getPlaylistTitle(String url) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--no-warnings", "--ignore-errors", "--flat-playlist", "--print", "playlist_title", url);
        try {
            String output = executor.run(command, "Failed to fetch playlist title");
            return output.trim();
        } catch (Exception e) {
            log.warn("Could not fetch playlist title for {}. Error: {}", url, e.getMessage());
            return "Unknown_Playlist";
        }
    }
}
