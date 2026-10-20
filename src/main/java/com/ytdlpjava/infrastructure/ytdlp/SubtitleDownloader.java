package com.ytdlpjava.infrastructure.ytdlp;

import com.ytdlpjava.core.ProcessExecutor;
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
                "--ignore-errors",
                "--newline",
                "--progress",
                "--write-subs",
                "--write-auto-sub",
                "--sub-lang", lang,
                "--sub-format", "vtt",
                "--no-continue",
                "--no-part",
                "--skip-download",
                "--output", outputBasename.replace("%", "%%"),
                videoUrl
        );

        log.info("Downloading subtitles for: {}", videoUrl);
        runResiliently(command, "Subtitle download failed");

        // yt-dlp appends .lang.ext (e.g., .en.vtt). Find the resulting file.
        Path basename = Path.of(outputBasename).toAbsolutePath();
        String prefix = basename.getFileName() + "." + lang + ".";
        try (var files = Files.list(basename.getParent())) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith(prefix)
                            && p.getFileName().toString().endsWith(".vtt"))
                    .findFirst()
                    .orElseThrow(() -> new IOException("No supported VTT subtitles found for " + outputBasename
                            + " (language: " + lang + ")"));
        }
    }
}
