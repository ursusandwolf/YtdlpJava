package com.ytdlpjava.downloader;

import com.ytdlpjava.core.ProcessExecutor;
import com.ytdlpjava.model.Downloader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractYoutubeService implements Downloader {
    protected final ProcessExecutor executor;

    protected String runResiliently(List<String> command, String errorMessage) throws IOException, InterruptedException {
        // 1. Try original command (default clients: often ios, android, web, tv)
        try {
            return executor.run(command, errorMessage);
        } catch (RuntimeException e) {
            log.warn("Default attempt failed: {}. Retrying with android/ios clients...", e.getMessage());
        }

        // 2. Try specifically with android/ios (often more reliable for just-ended streams)
        try {
            List<String> androidFallback = new ArrayList<>(command);
            androidFallback.add(1, "--extractor-args");
            androidFallback.add(2, "youtube:player_client=android,ios");
            return executor.run(androidFallback, errorMessage);
        } catch (RuntimeException e) {
            log.warn("Android/ios fallback failed. Retrying with web/mweb/tv clients...");
        }

        // 3. Try with web/mweb/tv as a last resort
        List<String> finalFallback = new ArrayList<>(command);
        finalFallback.add(1, "--extractor-args");
        finalFallback.add(2, "youtube:player_client=web,mweb,tv");
        return executor.run(finalFallback, errorMessage + " (All fallbacks failed)");
    }

    @Override
    public String getTitle(String videoUrl) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--no-warnings", "--ignore-errors", "--print", "title", videoUrl);
        try {
            return runResiliently(command, "Failed to fetch video title").trim();
        } catch (Exception e) {
            log.warn("Could not fetch video title for {}. Error: {}", videoUrl, e.getMessage());
            return "Unknown_Video_" + System.currentTimeMillis();
        }
    }

    public String getStreamUrl(String videoUrl) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--no-warnings", "--ignore-errors", "-f", "bestvideo[ext=mp4]/best[ext=mp4]/best", "-g", videoUrl);
        return runResiliently(command, "Failed to fetch stream URL");
    }

    public long getDuration(String videoUrl) throws IOException, InterruptedException {
        List<String> command = List.of("yt-dlp", "--no-warnings", "--ignore-errors", "--print", "%(duration)j", videoUrl);
        try {
            String output = runResiliently(command, "Failed to fetch video duration");
            return Long.parseLong(output);
        } catch (Exception e) {
            log.warn("Could not parse duration for {}, trying fallback. Error: {}", videoUrl, e.getMessage());
            List<String> fallbackCmd = List.of("yt-dlp", "--no-warnings", "--ignore-errors", "--print", "duration", videoUrl);
            try {
                String fallbackOutput = runResiliently(fallbackCmd, "Failed to fetch duration fallback");
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
            String output = runResiliently(command, "Failed to fetch playlist URLs");
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
            String output = runResiliently(command, "Failed to fetch playlist title");
            return output.trim();
        } catch (Exception e) {
            log.warn("Could not fetch playlist title for {}. Error: {}", url, e.getMessage());
            return "Unknown_Playlist";
        }
    }
}
