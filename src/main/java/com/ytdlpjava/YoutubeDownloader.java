package com.ytdlpjava;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class YoutubeDownloader extends AbstractYoutubeService {

    private final String lang;

    public YoutubeDownloader(String lang) {
        this.lang = lang;
    }

    @Override
    public Path download(String videoUrl, String outputBasename) throws IOException, InterruptedException {
        Path subtitlePath = Path.of(outputBasename + "." + lang + ".vtt");

        List<String> command = List.of(
                "yt-dlp",
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
}
