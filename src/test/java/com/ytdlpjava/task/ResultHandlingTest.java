package com.ytdlpjava.task;

import com.ytdlpjava.core.YtdlManager;
import com.ytdlpjava.infrastructure.filesystem.FilesystemTemporaryFileManager;
import com.ytdlpjava.model.Downloader;
import com.ytdlpjava.util.FilenameGenerator;
import com.ytdlpjava.model.FrameExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ResultHandlingTest {
    @TempDir Path tempDir;

    @Test
    void invalidScreenshotIntervalFailsBeforeDownload() {
        Downloader downloader = mock(Downloader.class);
        for (int interval : new int[]{0, -1}) {
            ScreenshotTask task = new ScreenshotTask(downloader, downloader, downloader, new FilenameGenerator(),
                    mock(FrameExtractor.class), new FilesystemTemporaryFileManager(), interval, List.of());
            assertThrows(IllegalArgumentException.class, () -> task.execute("video", tempDir));
        }
        verifyNoInteractions(downloader);
    }

    @Test
    void preservesSourceAndBothResultsOnNameCollision() throws Exception {
        Path source = Files.writeString(tempDir.resolve("source.txt"), "first");
        Path destination = tempDir.resolve("out");
        FileResultHandler handler = new FileResultHandler(destination);
        handler.handle("title", source);
        Files.writeString(source, "second");
        handler.handle("title", source);
        assertEquals("first", Files.readString(destination.resolve("source.txt")));
        assertEquals("second", Files.readString(destination.resolve("source (1).txt")));
        assertEquals("second", Files.readString(source));
    }

    @Test
    void concurrentCopiesNeverOverwriteEachOther() throws Exception {
        Path source = Files.writeString(tempDir.resolve("same.txt"), "content");
        Path destination = tempDir.resolve("out");
        FileResultHandler handler = new FileResultHandler(destination);
        try (var pool = Executors.newFixedThreadPool(4)) {
            List<Future<?>> copies = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                copies.add(pool.submit(() -> {
                    try { handler.handle("title", source); } catch (Exception e) { throw new RuntimeException(e); }
                }));
            }
            for (Future<?> copy : copies) copy.get();
        }
        try (var files = Files.list(destination)) {
            assertEquals(8, files.count());
        }
    }

    @Test
    void laterHandlersCanReadSourceAfterFileHandler() throws Exception {
        Path source = Files.writeString(tempDir.resolve("source.txt"), "content");
        AtomicBoolean read = new AtomicBoolean();
        TaskResultHandler observer = (title, path) -> {
            assertEquals("content", Files.readString(path));
            read.set(true);
        };
        ResultDispatcher.dispatch(List.of(new FileResultHandler(tempDir.resolve("ignored")), observer),
                "title", source, tempDir.resolve("actual"));
        assertTrue(read.get());
        assertTrue(Files.exists(source));
        assertTrue(Files.exists(tempDir.resolve("actual/source.txt")));
        assertFalse(Files.exists(tempDir.resolve("ignored")));
    }

    @Test
    void taskAttemptsRemainingHandlersAndCleansSourceOnFailure() throws Exception {
        Path source = Files.writeString(tempDir.resolve("source.mp4"), "content");
        Downloader downloader = mock(Downloader.class);
        when(downloader.getTitle("video")).thenReturn("title");
        when(downloader.download(eq("video"), anyString())).thenReturn(source);
        AtomicBoolean read = new AtomicBoolean();
        TaskResultHandler failing = (title, path) -> { throw new IOException("unavailable"); };
        TaskResultHandler observer = (title, path) -> {
            assertEquals("content", Files.readString(path));
            read.set(true);
        };
        VideoDownloadTask task = new VideoDownloadTask(downloader, downloader, new FilenameGenerator(),
                new FilesystemTemporaryFileManager(), List.of(failing, observer));
        Exception failure = assertThrows(Exception.class, () -> task.execute("video", tempDir.resolve("out")));
        assertEquals("unavailable", failure.getCause().getMessage());
        assertTrue(read.get());
        assertFalse(Files.exists(source));
    }

    @Test
    void realTaskSavesIntoEachPlaylistDirectory() throws Exception {
        Downloader downloader = mock(Downloader.class);
        String first = "playlist?list=one";
        String second = "playlist?list=two";
        when(downloader.getPlaylistUrls(first)).thenReturn(List.of("a", "b"));
        when(downloader.getPlaylistUrls(second)).thenReturn(List.of("c"));
        when(downloader.getPlaylistTitle(first)).thenReturn("One");
        when(downloader.getPlaylistTitle(second)).thenReturn("Two");
        when(downloader.getTitle(anyString())).thenReturn("same");
        Path source = tempDir.resolve("same.mp4");
        when(downloader.download(anyString(), anyString())).thenAnswer(call -> Files.writeString(source, call.getArgument(0)));
        Path out = tempDir.resolve("out");
        VideoDownloadTask task = new VideoDownloadTask(downloader, downloader, (title, length) -> title,
                new FilesystemTemporaryFileManager(), List.of(new FileResultHandler(out)));
        YtdlManager manager = new YtdlManager(task, downloader);
        manager.process(first, out);
        manager.process(second, out);
        assertEquals("a", Files.readString(out.resolve("One/same.mp4")));
        assertEquals("b", Files.readString(out.resolve("One/same (1).mp4")));
        assertEquals("c", Files.readString(out.resolve("Two/same.mp4")));
        assertFalse(Files.exists(out.resolve("same.mp4")));
        assertFalse(Files.exists(source));
    }
}
