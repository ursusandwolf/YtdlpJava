package com.ytdlpjava.cli;

import com.ytdlpjava.config.AppConfig;
import com.ytdlpjava.config.ApplicationFactory;
import com.ytdlpjava.core.ProcessExecutor;
import com.ytdlpjava.model.Downloader;
import com.ytdlpjava.model.FilenameProvider;
import com.ytdlpjava.model.VideoTask;
import com.ytdlpjava.util.LemmatizerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MainTest {
    @TempDir Path tempDir;

    static class Factory extends ApplicationFactory {
        final Downloader downloader = mock(Downloader.class);
        final VideoTask task = mock(VideoTask.class);
        boolean taskCreated;
        LemmatizerService service;

        @Override
        public Downloader createDownloader(AppConfig config, ProcessExecutor executor) { return downloader; }

        @Override
        public VideoTask createTask(AppConfig config, Downloader downloader, FilenameProvider names, ProcessExecutor executor) {
            taskCreated = true;
            service = createLemmatizerService();
            return task;
        }
    }

    @Test
    void invalidArgumentsFailBeforeInfrastructureStarts() {
        for (String[] options : List.of(
                new String[]{"-i", "0"}, new String[]{"-i", "-10"},
                new String[]{"-t", "invalid"}, new String[]{"--format", "wav"},
                new String[]{"--quality", "10"}, new String[]{"--quality", "bad"})) {
            Factory factory = new Factory();
            String[] args = new String[options.length + 1];
            args[0] = "video";
            System.arraycopy(options, 0, args, 1, options.length);
            assertEquals(2, Main.run(args, factory));
            assertFalse(factory.taskCreated);
            verifyNoInteractions(factory.downloader, factory.task);
        }
    }

    @Test
    void helpReturnsSuccessWithoutInfrastructure() {
        Factory factory = new Factory();
        assertEquals(0, Main.run(new String[]{"--help"}, factory));
        assertFalse(factory.taskCreated);
    }

    @Test
    void returnsNonzeroAndContinuesRemainingUrlsAfterPartialFailure() throws Exception {
        Factory factory = new Factory();
        when(factory.downloader.getPlaylistUrls("first")).thenReturn(List.of("bad", "good"));
        when(factory.downloader.getPlaylistTitle("first")).thenReturn("NA");
        when(factory.downloader.getPlaylistUrls("second")).thenReturn(List.of("last"));
        doThrow(new java.io.IOException("failed")).when(factory.task).execute(eq("bad"), any(Path.class));
        assertEquals(1, Main.run(new String[]{"first", "second", "-o", tempDir.toString()}, factory));
        verify(factory.task).execute(eq("good"), any(Path.class));
        verify(factory.task).execute(eq("last"), any(Path.class));
        assertThrows(IllegalStateException.class, () -> factory.service.getLemma("word", LemmatizerService.Language.EN));
    }

    @Test
    void reportsSingleAndTotalFailures() throws Exception {
        Factory factory = new Factory();
        when(factory.downloader.getPlaylistUrls("video")).thenReturn(List.of("bad"));
        doThrow(new java.io.IOException("failed")).when(factory.task).execute(eq("bad"), any(Path.class));
        assertEquals(1, Main.run(new String[]{"video", "-o", tempDir.toString()}, factory));
    }

    @Test
    void emptyPlaylistIsAnError() throws Exception {
        Factory factory = new Factory();
        when(factory.downloader.getPlaylistUrls("empty")).thenReturn(List.of());
        assertEquals(1, Main.run(new String[]{"empty", "-o", tempDir.toString()}, factory));
        verifyNoInteractions(factory.task);
    }

    @Test
    void closesResourcesAfterSuccessfulBatch() throws Exception {
        Factory factory = new Factory();
        when(factory.downloader.getPlaylistUrls("video")).thenReturn(List.of("good"));
        assertEquals(0, Main.run(new String[]{"video", "-o", tempDir.toString()}, factory));
        assertThrows(IllegalStateException.class, () -> factory.service.getLemma("word", LemmatizerService.Language.EN));
        assertThrows(IllegalStateException.class, factory::createLemmatizerService);
    }

    @Test
    void closesResourcesWhenTaskConstructionFails() throws Exception {
        Factory factory = new Factory() {
            @Override
            public VideoTask createTask(AppConfig config, Downloader downloader, FilenameProvider names, ProcessExecutor executor) {
                service = createLemmatizerService();
                throw new IllegalStateException("construction failed");
            }
        };
        assertEquals(1, Main.run(new String[]{"video"}, factory));
        assertThrows(IllegalStateException.class, () -> factory.service.getLemma("word", LemmatizerService.Language.EN));
    }
}
