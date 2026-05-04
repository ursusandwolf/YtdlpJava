package com.ytdlpjava;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class AudioDownloader extends AbstractYoutubeService {
    private final String format;
    private final String quality;

    public AudioDownloader(String format, String quality) {
        this.format = format;
        this.quality = quality;
    }

    @Override
    public Path download(String videoUrl, String outputBasename) throws IOException, InterruptedException {
        List<String> command = List.of(
                "yt-dlp",
                "-x",
                "--audio-format", format,
                "--audio-quality", quality,
                "--output", outputBasename + ".%(ext)s",
                videoUrl
        );

        log.info("Downloading audio ({}, q={}) for: {}", format, quality, videoUrl);
        runCommand(command, "Audio download failed");

        // yt-dlp might use a slightly different extension than 'format' (e.g. m4a -> mp4 then converted)
        // We look for a file starting with outputBasename
        try (Stream<Path> files = Files.list(Path.of("."))) {
             return files
                     .filter(p -> p.getFileName().toString().startsWith(outputBasename))
                     .findFirst()
                     .orElseThrow(() -> new IOException("Downloaded audio file not found for: " + outputBasename));
        }
    }
}
