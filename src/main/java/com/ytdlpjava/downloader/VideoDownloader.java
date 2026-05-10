package com.ytdlpjava.downloader;

import com.ytdlpjava.core.ProcessExecutor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class VideoDownloader extends AbstractYoutubeService {

    public VideoDownloader(ProcessExecutor executor) {
        super(executor);
    }

    @Override
    public Path download(String videoUrl, String outputBasename) throws IOException, InterruptedException {
        List<String> command = List.of(
                "yt-dlp",
                "--no-warnings",
                "--ignore-errors",
                "--newline",
                "--progress",
                "-f", "bestvideo[height<=720][ext=mp4]+bestaudio[ext=m4a]/best[height<=720][ext=mp4]/best",
                "--output", outputBasename + ".%(ext)s",
                "--print", "after_move:filepath",
                videoUrl
        );

        log.info("Downloading video (720p max) for: {}", videoUrl);
        String filePath = runResiliently(command, "Video download failed");
        return Path.of(filePath);
    }
}
