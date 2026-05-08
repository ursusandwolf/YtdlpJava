package com.ytdlpjava;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class MetadataDownloader extends AbstractYoutubeService {

    public MetadataDownloader(ProcessExecutor executor) {
        super(executor);
    }

    @Override
    public Path download(String url, String outputBasename) throws IOException, InterruptedException {
        List<String> command = List.of(
            "yt-dlp",
            "--no-warnings",
            "--skip-download",
            "--write-description",
            "--write-info-json",
            "-o", outputBasename,
            url
        );
        log.info("Extracting metadata for: {}", url);
        executor.run(command, "Failed to extract metadata");
        
        Path jsonPath = Path.of(outputBasename + ".info.json");
        Path descPath = Path.of(outputBasename + ".description");
        
        if (Files.exists(jsonPath)) {
            return jsonPath;
        } else if (Files.exists(descPath)) {
            return descPath;
        }
        throw new IOException("Failed to find extracted metadata files");
    }
}
