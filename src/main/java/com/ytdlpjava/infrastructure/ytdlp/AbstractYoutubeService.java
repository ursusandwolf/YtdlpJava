package com.ytdlpjava.infrastructure.ytdlp;

import com.ytdlpjava.core.ProcessExecutor;
import com.ytdlpjava.model.Downloader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractYoutubeService implements Downloader {
    protected final ProcessExecutor executor;
    private final YtdlpOutputParser outputParser = new YtdlpOutputParser();

    protected String runResiliently(List<String> command, String errorMessage) throws IOException, InterruptedException {
        List<String> baseCommand = new ArrayList<>(command);
        if (!baseCommand.isEmpty() && "yt-dlp".equals(baseCommand.get(0)) && !baseCommand.contains("--js-runtimes")) {
            baseCommand.add(1, "--js-runtimes");
            baseCommand.add(2, "node");
        }

        // 1. Try original command with retries for transient errors
        try {
            return runWithRetries(baseCommand, errorMessage, 3);
        } catch (RuntimeException e) {
            log.warn("Default attempt failed: {}. Retrying with android client...", e.getMessage());
        }

        // 2. Try specifically with android
        try {
            List<String> androidFallback = new ArrayList<>(baseCommand);
            androidFallback.add(1, "--extractor-args");
            androidFallback.add(2, "youtube:player_client=android");
            return runWithRetries(androidFallback, errorMessage, 2);
        } catch (RuntimeException e) {
            log.warn("Android fallback failed. Retrying with mweb client...");
        }

        // 3. Try with mweb client
        try {
            List<String> mwebFallback = new ArrayList<>(baseCommand);
            mwebFallback.add(1, "--extractor-args");
            mwebFallback.add(2, "youtube:player_client=mweb");
            return runWithRetries(mwebFallback, errorMessage, 2);
        } catch (RuntimeException e) {
            log.warn("Mweb fallback failed. Retrying with embedded client...");
        }

        // 4. Try with embedded client
        try {
            List<String> embeddedFallback = new ArrayList<>(baseCommand);
            embeddedFallback.add(1, "--extractor-args");
            embeddedFallback.add(2, "youtube:player_client=embedded");
            return runWithRetries(embeddedFallback, errorMessage, 2);
        } catch (RuntimeException e) {
            log.warn("Embedded fallback failed. Retrying with config-skip mode...");
        }

        // 5. Last resort desperation fallback
        List<String> desperateFallback = new ArrayList<>(baseCommand);
        desperateFallback.add(1, "--extractor-args");
        desperateFallback.add(2, "youtube:skip=dash,hls;player_skip=configs");
        desperateFallback.add("--skip-unavailable-fragments");
        
        if (baseCommand.contains("--write-auto-sub")) {
            int subFormatIdx = desperateFallback.indexOf("--sub-format");
            if (subFormatIdx != -1) {
                desperateFallback.set(subFormatIdx + 1, "vtt/json3/srv1/srv2/srv3/best");
            }
        }
        
        return runWithRetries(desperateFallback, errorMessage + " (All fallbacks failed)", 1);
    }

    private String runWithRetries(List<String> command, String errorMessage, int maxRetries) throws IOException, InterruptedException {
        int attempt = 0;
        while (true) {
            try {
                return executor.run(command, errorMessage);
            } catch (RuntimeException e) {
                attempt++;
                if (attempt >= maxRetries || !isTransientError(e.getMessage())) {
                    throw e;
                }
                long sleepMs = (long) Math.pow(2, attempt) * 1000;
                log.warn("Transient error detected: {}. Retrying in {}ms (Attempt {}/{})", e.getMessage(), sleepMs, attempt, maxRetries);
                Thread.sleep(sleepMs);
            }
        }
    }

    private boolean isTransientError(String message) {
        if (message == null) return false;
        String lower = message.toLowerCase();
        return lower.contains("timed out") || 
               lower.contains("connection reset") || 
               lower.contains("503") || 
               lower.contains("500") || 
               lower.contains("sign in to confirm your age") ||
               lower.contains("too many requests");
    }

    protected Path extractDownloadedPath(String output, String errorMessage) throws IOException {
        return outputParser.findLastExistingFilePath(output)
                .orElseThrow(() -> new IOException(errorMessage + ": downloaded file path not found in yt-dlp output"));
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
            return output.lines().findFirst().orElse("Unknown_Playlist").trim();
        } catch (Exception e) {
            log.warn("Could not fetch playlist title for {}. Error: {}", url, e.getMessage());
            return "Unknown_Playlist";
        }
    }
}
