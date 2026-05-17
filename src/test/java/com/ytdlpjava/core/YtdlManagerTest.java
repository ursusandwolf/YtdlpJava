package com.ytdlpjava.core;

import com.ytdlpjava.model.Downloader;
import com.ytdlpjava.model.VideoTask;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class YtdlManagerTest {

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
