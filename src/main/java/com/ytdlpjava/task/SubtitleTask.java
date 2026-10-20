package com.ytdlpjava.task;

import com.ytdlpjava.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SubtitleTask implements VideoTask {
    private final MediaDownloader downloader;
    private final TitleProvider titleProvider;
    private final ContentProcessor processor;
    private final FilenameProvider filenameProvider;
    private final TemporaryFileManager temporaryFileManager;
    private final List<TaskResultHandler> resultHandlers;

    @Override
    public void execute(String url, Path outputDir) throws Exception {
        String title = titleProvider.getTitle(url);
        String basename = filenameProvider.buildFilename(title, 60);

        try (DownloadWorkspace workspace = new DownloadWorkspace()) {
            Path downloadedFile = workspace.download(downloader, url, basename);
            Path tempSubtitleFile = null;
            try {
                String result = processor.process(downloadedFile);
                tempSubtitleFile = temporaryFileManager.writeTempFile(basename, ".md", result);

                ResultDispatcher.dispatch(resultHandlers, title, tempSubtitleFile, outputDir);
            } finally {
                temporaryFileManager.deleteIfExists(downloadedFile);
                log.debug("Deleted temporary VTT file: {}", downloadedFile);
                temporaryFileManager.deleteIfExists(tempSubtitleFile);
            }
        }
    }

    @Override
    public FilenameProvider getFilenameProvider() {
        return filenameProvider;
    }
}
