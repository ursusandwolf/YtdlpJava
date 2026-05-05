package com.ytdlpjava;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class AudioDownloader extends AbstractYoutubeService {
    private final String format;
    private final String quality;

    public AudioDownloader(ProcessExecutor executor, String format, String quality) {
        super(executor);
        this.format = format;
        this.quality = quality;
    }

    @Override
    public Path download(String videoUrl, String outputBasename) throws IOException, InterruptedException {
        List<String> command = List.of(
                "yt-dlp",
                "--no-warnings",
                "--newline",
                "--progress",
                "-x",
                "--audio-format", format,
                "--audio-quality", quality,
                "--output", outputBasename + ".%(ext)s",
                "--print", "after_move:filepath",
                videoUrl
        );

        log.info("Downloading audio ({}, q={}) for: {}", format, quality, videoUrl);
        String filePath = executor.run(command, "Audio download failed");
        return Path.of(filePath);
    }
}
