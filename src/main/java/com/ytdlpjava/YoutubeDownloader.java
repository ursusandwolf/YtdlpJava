package com.ytdlpjava;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class YoutubeDownloader extends AbstractYoutubeService {

    private final String lang;
    private final boolean downloadVideo;

    public YoutubeDownloader(String lang) {
        this(lang, false);
    }

    public YoutubeDownloader(String lang, boolean downloadVideo) {
        this.lang = lang;
        this.downloadVideo = downloadVideo;
    }

    @Override
    public Path download(String videoUrl, String outputBasename) throws IOException, InterruptedException {
        if (downloadVideo) {
            return downloadActualVideo(videoUrl, outputBasename);
        } else {
            return downloadSubtitles(videoUrl, outputBasename);
        }
    }

    private Path downloadSubtitles(String videoUrl, String outputBasename) throws IOException, InterruptedException {
        Path subtitlePath = Path.of(outputBasename + "." + lang + ".vtt");

        List<String> command = List.of(
                "yt-dlp",
                "--no-warnings",
                "--write-auto-sub",
                "--sub-lang", lang,
                "--skip-download",
                "--output", outputBasename,
                videoUrl
        );

        log.info("Downloading subtitles for: {}", videoUrl);
        runCommand(command, "yt-dlp subtitle download failed");

        if (!Files.exists(subtitlePath)) {
            throw new IOException("Subtitle file not found: " + subtitlePath);
        }

        return subtitlePath;
    }

    private Path downloadActualVideo(String videoUrl, String outputBasename) throws IOException, InterruptedException {
        // We download in 720p or lower for speed, mp4 for compatibility
        List<String> command = List.of(
                "yt-dlp",
                "--no-warnings",
                "-f", "bestvideo[height<=720][ext=mp4]+bestaudio[ext=m4a]/best[height<=720][ext=mp4]/best",
                "--output", outputBasename + ".%(ext)s",
                videoUrl
        );

        log.info("Downloading video for screenshots (720p max): {}", videoUrl);
        runCommand(command, "Video download failed");

        // Find the downloaded file (ext might vary if 720p mp4 wasn't found)
        try (var files = Files.list(Path.of("."))) {
            return files
                    .filter(p -> p.getFileName().toString().startsWith(outputBasename))
                    .findFirst()
                    .orElseThrow(() -> new IOException("Downloaded video file not found for: " + outputBasename));
        }
    }
}
