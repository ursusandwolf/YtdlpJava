package com.ytdlpjava.infrastructure.ytdlp;

import com.ytdlpjava.core.ProcessExecutor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class VideoDownloader extends AbstractYoutubeService {
    public static final int DEFAULT_MAX_HEIGHT = 720;

    private final int maxHeight;

    public VideoDownloader(ProcessExecutor executor) {
        this(executor, DEFAULT_MAX_HEIGHT);
    }

    public VideoDownloader(ProcessExecutor executor, int maxHeight) {
        super(executor);
        this.maxHeight = maxHeight;
    }

    /** @param maxHeight pixels; {@code <= 0} means no height cap */
    static String formatSelector(int maxHeight) {
        if (maxHeight <= 0) {
            return "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best";
        }
        String limit = "height<=" + maxHeight;
        return "bestvideo[" + limit + "][ext=mp4]+bestaudio[ext=m4a]/best[" + limit + "][ext=mp4]/best[" + limit + "]";
    }

    @Override
    public Path download(String videoUrl, String outputBasename) throws IOException, InterruptedException {
        String selector = formatSelector(maxHeight);
        List<String> command = List.of(
                "yt-dlp",
                "--no-warnings",
                "--ignore-errors",
                "--newline",
                "--progress",
                "--no-continue",
                "-f", selector,
                "--output", outputBasename.replace("%", "%%") + ".%(ext)s",
                "--print", "after_move:filepath",
                videoUrl
        );

        if (maxHeight <= 0) {
            log.info("Downloading video (no height limit) for: {}", videoUrl);
        } else {
            log.info("Downloading video (max {}p) for: {}", maxHeight, videoUrl);
        }
        String output = runResiliently(command, "Video download failed");
        return extractDownloadedPath(output, "Video download failed");
    }
}
