package com.ytdlpjava.infrastructure.ytdlp;

import com.ytdlpjava.core.ProcessExecutor;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VideoDownloaderTest {

    @Test
    void defaultSelectorCapsEveryFallbackAt720() {
        String selector = VideoDownloader.formatSelector(720);
        assertTrue(selector.contains("height<=720"));
        assertFalse(selector.endsWith("/best"));
        assertEquals(
                "bestvideo[height<=720][ext=mp4]+bestaudio[ext=m4a]/best[height<=720][ext=mp4]/best[height<=720]",
                selector);
    }

    @Test
    void unlimitedSelectorAllowsBestWithoutHeight() {
        assertEquals(
                "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best",
                VideoDownloader.formatSelector(0));
        assertEquals(
                "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best",
                VideoDownloader.formatSelector(-5));
    }

    @Test
    void downloadUsesConfiguredMaxHeight() throws Exception {
        AtomicReference<String> format = new AtomicReference<>();
        Path file = Files.createTempFile("video-", ".mp4");
        try {
            var executor = new ProcessExecutor() {
                @Override
                public String run(List<String> command, String message) {
                    int index = command.indexOf("-f");
                    format.set(command.get(index + 1));
                    return file.toAbsolutePath().toString();
                }
            };
            new VideoDownloader(executor, 1080).download("video", file.getParent().resolve("out").toString());
            assertEquals(VideoDownloader.formatSelector(1080), format.get());
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
