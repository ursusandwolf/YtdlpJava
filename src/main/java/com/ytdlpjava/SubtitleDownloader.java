package com.ytdlpjava;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class SubtitleDownloader extends AbstractYoutubeService {
    private final String lang;

    public SubtitleDownloader(ProcessExecutor executor, String lang) {
        super(executor);
        this.lang = lang;
    }

    @Override
    public Path download(String videoUrl, String outputBasename) throws IOException, InterruptedException {
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
        executor.run(command, "Subtitle download failed");

        // yt-dlp appends .lang.ext (e.g., .en.vtt). Find the resulting file.
        try (var files = Files.list(Path.of("."))) {
            return files
                    .filter(p -> p.getFileName().toString().startsWith(outputBasename + "." + lang))
                    .findFirst()
                    .orElseThrow(() -> new IOException("Subtitle file not found for " + outputBasename));
        }
    }
}
