package com.ytdlpjava.task;

import com.ytdlpjava.infrastructure.filesystem.FilesystemTemporaryFileManager;
import com.ytdlpjava.model.MediaDownloader;
import com.ytdlpjava.model.VideoTask;
import com.ytdlpjava.util.FilenameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DownloadIsolationTest {
    @TempDir Path output;

    private List<VideoTask> tasks(MediaDownloader downloader, TaskResultHandler handler) {
        var names = new FilenameGenerator();
        var files = new FilesystemTemporaryFileManager();
        var handlers = List.of(handler);
        return List.of(
                new VideoDownloadTask(downloader, url -> "same", names, files, handlers),
                new AudioTask(downloader, url -> "same", names, files, handlers),
                new MetadataTask(downloader, url -> "same", names, files, handlers),
                new SubtitleTask(downloader, url -> "same", path -> Files.readString(path), names, files, handlers),
                new ScreenshotTask(downloader, url -> "same", url -> 1, names,
                        (video, timestamp, image) -> Files.writeString(image, "frame"), files, 60, handlers));
    }

    @Test
    void eachTaskUsesItsOwnDirectoryAndCleansPartialDownloads() throws Exception {
        AtomicReference<Path> downloadDirectory = new AtomicReference<>();
        MediaDownloader downloader = (url, basename) -> {
            Path directory = Path.of(basename).toAbsolutePath().getParent();
            downloadDirectory.set(directory);
            assertNotEquals(Path.of(".").toAbsolutePath().normalize(), directory);
            Files.writeString(Path.of(basename + ".part"), "partial");
            Files.writeString(Path.of(basename + ".description"), "description");
            throw new IOException("download failed");
        };
        for (VideoTask task : tasks(downloader, (title, path) -> fail("No result expected"))) {
            IOException failure = assertThrows(IOException.class, () -> task.execute("video", output));
            assertEquals("download failed", failure.getMessage());
            assertFalse(Files.exists(downloadDirectory.get()));
        }
    }

    @Test
    void rejectsExternalResultAndPreservesItWhenSavingFails() throws Exception {
        Path existing = Files.writeString(output.resolve("existing.mp4"), "user content");
        for (VideoTask task : tasks((url, basename) -> existing,
                (title, path) -> { throw new IOException("save failed"); })) {
            assertThrows(IOException.class, () -> task.execute("video", output));
            assertEquals("user content", Files.readString(existing));
        }
    }

    @Test
    void eachTaskCleansOwnedFilesAfterSuccessfulSave() throws Exception {
        AtomicReference<Path> downloadDirectory = new AtomicReference<>();
        MediaDownloader downloader = (url, basename) -> {
            Path file = Path.of(basename + ".mp4");
            downloadDirectory.set(file.getParent());
            Files.writeString(Path.of(basename + ".description"), "description");
            return Files.writeString(file, "content");
        };
        for (VideoTask task : tasks(downloader, new FileResultHandler(output))) {
            task.execute("video", output);
            assertFalse(Files.exists(downloadDirectory.get()));
        }
        assertEquals("content", Files.readString(output.resolve("same.mp4")));
    }

    @Test
    void eachTaskCleansOwnedFilesWhenSavingFails() throws Exception {
        AtomicReference<Path> directory = new AtomicReference<>();
        AtomicReference<Path> handledFile = new AtomicReference<>();
        MediaDownloader downloader = (url, basename) -> {
            Path file = Path.of(basename + ".mp4");
            directory.set(file.getParent());
            Files.writeString(Path.of(basename + ".description"), "description");
            return Files.writeString(file, "content");
        };
        TaskResultHandler handler = (title, file) -> {
            handledFile.set(file);
            throw new IOException("save failed");
        };
        for (VideoTask task : tasks(downloader, handler)) {
            Exception failure = assertThrows(Exception.class, () -> task.execute("video", output));
            assertEquals("save failed", failure.getCause().getMessage());
            assertFalse(Files.exists(directory.get()));
            assertFalse(Files.exists(handledFile.get()));
        }
    }

    @Test
    void rejectsBasenamesThatEscapeDownloadDirectory() throws Exception {
        for (String basename : List.of(".", "..", "../outside", output.toAbsolutePath().toString())) {
            try (var workspace = new DownloadWorkspace()) {
                assertThrows(IOException.class, () -> workspace.download(
                        (url, name) -> { fail("Downloader must not run"); return null; }, "video", basename));
            }
        }
    }
}
