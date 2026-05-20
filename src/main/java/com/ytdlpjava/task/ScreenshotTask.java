package com.ytdlpjava.task;

import com.ytdlpjava.model.DurationProvider;
import com.ytdlpjava.model.FilenameProvider;
import com.ytdlpjava.model.FrameExtractor;
import com.ytdlpjava.model.MediaDownloader;
import com.ytdlpjava.model.TemporaryFileManager;
import com.ytdlpjava.model.TitleProvider;
import com.ytdlpjava.model.VideoTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ScreenshotTask implements VideoTask {
    private final MediaDownloader downloader;
    private final TitleProvider titleProvider;
    private final DurationProvider durationProvider;
    private final FilenameProvider filenameProvider;
    private final FrameExtractor frameExtractor;
    private final TemporaryFileManager temporaryFileManager;
    private final int intervalSeconds;
    private final List<TaskResultHandler> resultHandlers;

    @Override
    public void execute(String url, Path outputDir) throws Exception {
        String title = titleProvider.getTitle(url);
        String basename = filenameProvider.buildFilename(title, 60);

        log.info("Downloading video for reliable screenshot extraction: {}", title);
        Path videoFile = downloader.download(url, basename);
        
        try {
            long duration = durationProvider.getDuration(url);
            log.info("Capturing screenshots for '{}' (Duration: {}s, Interval: {}s)", title, duration, intervalSeconds);

            for (long ts = 0; ts < duration; ts += intervalSeconds) {
                Path tempScreenshot = temporaryFileManager.createTempFile(String.format("%s_%05d", basename, ts), ".jpg");
                
                log.debug("Capturing frame at {} -> {}", ts, tempScreenshot.getFileName());

                try {
                    frameExtractor.extractFrame(videoFile, ts, tempScreenshot);
                    for (TaskResultHandler handler : resultHandlers) {
                        handler.handle(title + " (screenshot " + ts + "s)", tempScreenshot);
                    }
                } finally {
                    temporaryFileManager.deleteIfExists(tempScreenshot);
                }
            }
            log.info("✅ Screenshot capture complete.");
        } finally {
            temporaryFileManager.deleteIfExists(videoFile);
            log.debug("Deleted temporary video file: {}", videoFile);
        }
    }

    @Override
    public FilenameProvider getFilenameProvider() {
        return filenameProvider;
    }
}
