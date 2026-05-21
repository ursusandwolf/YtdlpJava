package com.ytdlpjava.infrastructure.ytdlp;

import com.ytdlpjava.core.ProcessExecutor;
import com.ytdlpjava.model.Downloader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

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

        List<FallbackStrategy> strategies = List.of(
            new FallbackStrategy("Default", cmd -> cmd, 3),
            new FallbackStrategy("Android", cmd -> addExtractorArgs(cmd, "youtube:player_client=android"), 2),
            new FallbackStrategy("Mweb", cmd -> addExtractorArgs(cmd, "youtube:player_client=mweb"), 2),
            new FallbackStrategy("Embedded", cmd -> addExtractorArgs(cmd, "youtube:player_client=embedded"), 2),
            new FallbackStrategy("Desperate", this::applyDesperateFallback, 1)
        );

        for (int i = 0; i < strategies.size(); i++) {
            FallbackStrategy strategy = strategies.get(i);
            boolean isLast = (i == strategies.size() - 1);
            try {
                return runWithRetries(strategy.mutator().apply(baseCommand), 
                                     isLast ? errorMessage + " (All fallbacks failed)" : errorMessage, 
                                     strategy.retries());
            } catch (RuntimeException e) {
                if (isLast) throw e;
                log.warn("{} attempt failed: {}. Retrying with next strategy...", strategy.name(), e.getMessage());
            }
        }
        throw new RuntimeException(errorMessage);
    }

    private List<String> addExtractorArgs(List<String> cmd, String args) {
        List<String> c = new ArrayList<>(cmd);
        c.add(1, "--extractor-args");
        c.add(2, args);
        return c;
    }

    private List<String> applyDesperateFallback(List<String> cmd) {
        List<String> c = addExtractorArgs(cmd, "youtube:skip=dash,hls;player_skip=configs");
        c.add("--skip-unavailable-fragments");
        if (cmd.contains("--write-auto-sub")) {
            int subFormatIdx = c.indexOf("--sub-format");
            if (subFormatIdx != -1 && subFormatIdx + 1 < c.size()) {
                c.set(subFormatIdx + 1, "vtt/json3/srv1/srv2/srv3/best");
            }
        }
        return c;
    }

    private record FallbackStrategy(String name, UnaryOperator<List<String>> mutator, int retries) {}

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
