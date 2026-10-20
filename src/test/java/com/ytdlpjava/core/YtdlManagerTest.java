package com.ytdlpjava.core;

import com.ytdlpjava.model.Downloader;
import com.ytdlpjava.model.VideoTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.ytdlpjava.util.FilenameGenerator;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.*;

class YtdlManagerTest {

    @Test
    void dotOnlyPlaylistTitleStaysInsideOutputDirectory(@TempDir Path directory) throws Exception {
        VideoTask task = Mockito.mock(VideoTask.class);
        Downloader downloader = Mockito.mock(Downloader.class);
        when(task.getFilenameProvider()).thenReturn(new FilenameGenerator());
        when(downloader.getPlaylistUrls("playlist?list=one")).thenReturn(List.of("video"));
        when(downloader.getPlaylistTitle(anyString())).thenReturn("..");
        new YtdlManager(task, downloader).process("playlist?list=one", directory);
        verify(task).execute("video", directory.resolve("video"));
    }

    @Test
    void rejectsFilenameProviderReturningEscapingPaths(@TempDir Path directory) throws Exception {
        for (String name : List.of("..", "../escaped", directory.resolve("escaped").toAbsolutePath().toString())) {
            VideoTask task = Mockito.mock(VideoTask.class);
            Downloader downloader = Mockito.mock(Downloader.class);
            when(task.getFilenameProvider()).thenReturn((title, limit) -> name);
            when(downloader.getPlaylistUrls("playlist?list=one")).thenReturn(List.of("video"));
            when(downloader.getPlaylistTitle(anyString())).thenReturn("title");
            assertThrows(YtdlManagerException.class,
                    () -> new YtdlManager(task, downloader).process("playlist?list=one", directory.resolve("out")));
            verify(task, never()).execute(anyString(), any(Path.class));
        }
    }

    @Test
    void testProcessThrowsRuntimeExceptionOnPlaylistFetchFailure() throws Exception {
        VideoTask mockTask = Mockito.mock(VideoTask.class);
        Downloader mockDownloader = Mockito.mock(Downloader.class);
        
        // Setup: Force exception on playlist fetch
        when(mockDownloader.getPlaylistUrls("invalid-url")).thenThrow(new RuntimeException("Network error"));
        
        YtdlManager manager = new YtdlManager(mockTask, mockDownloader);
        
        // This confirms the current behavior (YtdlManagerException)
        assertThrows(YtdlManagerException.class, () -> manager.process("invalid-url", Path.of(".")));
    }
}
