package com.ytdlpjava.config;

import com.ytdlpjava.core.ProcessExecutor;
import com.ytdlpjava.model.Downloader;
import com.ytdlpjava.model.VideoTask;
import com.ytdlpjava.task.DownloadAndDispatchTask;
import com.ytdlpjava.task.ScreenshotTask;
import com.ytdlpjava.task.SubtitleTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationFactoryTest {

    @Test
    void createsSharedDownloadTaskForAudioVideoMetadata() {
        try (ApplicationFactory factory = new ApplicationFactory()) {
            ProcessExecutor executor = factory.createProcessExecutor();
            for (String type : new String[]{"audio", "video", "metadata"}) {
                AppConfig config = config(type);
                Downloader downloader = factory.createDownloader(config, executor);
                VideoTask task = factory.createTask(config, downloader, factory.createFilenameProvider(), executor);
                assertInstanceOf(DownloadAndDispatchTask.class, task);
            }
        }
    }

    @Test
    void createsSpecializedTasksForSubtitlesAndScreenshots() {
        try (ApplicationFactory factory = new ApplicationFactory()) {
            ProcessExecutor executor = factory.createProcessExecutor();

            AppConfig sub = config("sub");
            assertInstanceOf(SubtitleTask.class,
                    factory.createTask(sub, factory.createDownloader(sub, executor),
                            factory.createFilenameProvider(), executor));

            AppConfig shot = config("screenshot");
            shot.setInterval(30);
            assertInstanceOf(ScreenshotTask.class,
                    factory.createTask(shot, factory.createDownloader(shot, executor),
                            factory.createFilenameProvider(), executor));
        }
    }

    @Test
    void rejectsUnknownTaskType() {
        try (ApplicationFactory factory = new ApplicationFactory()) {
            AppConfig config = config("unknown");
            ProcessExecutor executor = factory.createProcessExecutor();
            Downloader downloader = factory.createDownloader(config, executor);
            assertThrows(IllegalArgumentException.class,
                    () -> factory.createTask(config, downloader, factory.createFilenameProvider(), executor));
        }
    }

    private static AppConfig config(String type) {
        AppConfig config = new AppConfig();
        config.setType(type);
        config.setOutputDir("out");
        return config;
    }
}
