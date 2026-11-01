package com.ytdlpjava.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalToolsPreflightTest {
    @Mock ProcessExecutor executor;

    @Test
    void subRequiresYtdlpAndNodeOnly() throws Exception {
        when(executor.run(anyList(), anyString(), anyLong(), any())).thenReturn("ok");
        assertDoesNotThrow(() -> new ExternalToolsPreflight(executor).verify("sub"));
        verify(executor).run(eq(List.of("yt-dlp", "--version")), anyString(), eq(30L), eq(TimeUnit.SECONDS));
        verify(executor).run(eq(List.of("node", "--version")), anyString(), eq(30L), eq(TimeUnit.SECONDS));
        verify(executor, never()).run(eq(List.of("ffmpeg", "-version")), anyString(), anyLong(), any());
    }

    @Test
    void videoRequiresFfmpeg() throws Exception {
        when(executor.run(anyList(), anyString(), anyLong(), any())).thenReturn("ok");
        assertDoesNotThrow(() -> new ExternalToolsPreflight(executor).verify("video"));
        verify(executor).run(eq(List.of("ffmpeg", "-version")), anyString(), eq(30L), eq(TimeUnit.SECONDS));
    }

    @Test
    void missingToolBecomesIllegalArgument() throws Exception {
        when(executor.run(eq(List.of("yt-dlp", "--version")), anyString(), anyLong(), any()))
                .thenThrow(new IOException("Cannot run program \"yt-dlp\""));
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> new ExternalToolsPreflight(executor).verify("metadata"));
        assertTrue(failure.getMessage().contains("yt-dlp"));
        assertTrue(failure.getMessage().contains("PATH"));
    }
}
