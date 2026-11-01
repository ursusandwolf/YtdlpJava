package com.ytdlpjava.task;

import com.ytdlpjava.model.FilenameProvider;
import com.ytdlpjava.model.MediaDownloader;
import com.ytdlpjava.model.TemporaryFileManager;
import com.ytdlpjava.model.TitleProvider;
import com.ytdlpjava.model.VideoTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;

/** Shared flow for audio, video and metadata: download once, dispatch, delete temp. */
@Slf4j
@RequiredArgsConstructor
public class DownloadAndDispatchTask implements VideoTask {
    private final MediaDownloader downloader;
    private final TitleProvider titleProvider;
    private final FilenameProvider filenameProvider;
    private final TemporaryFileManager temporaryFileManager;
    private final List<TaskResultHandler> resultHandlers;
    private final String actionLabel;

    @Override
    public void execute(String url, Path outputDir) throws Exception {
        String title = titleProvider.getTitle(url);
        String basename = filenameProvider.buildFilename(title, 60);
        log.info("Starting {} for: {}", actionLabel, title);
        try (DownloadWorkspace workspace = new DownloadWorkspace()) {
            Path downloadedFile = workspace.download(downloader, url, basename);
            try {
                ResultDispatcher.dispatch(resultHandlers, title, downloadedFile, outputDir);
            } finally {
                temporaryFileManager.deleteIfExists(downloadedFile);
            }
        }
    }

    @Override
    public FilenameProvider getFilenameProvider() {
        return filenameProvider;
    }
}
