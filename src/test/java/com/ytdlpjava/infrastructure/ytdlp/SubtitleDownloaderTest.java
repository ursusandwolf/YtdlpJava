package com.ytdlpjava.infrastructure.ytdlp;

import com.ytdlpjava.core.ProcessExecutor;
import com.ytdlpjava.core.ProcessExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class SubtitleDownloaderTest {
    @Test
    void everyFallbackRequestsOnlyVtt() throws Exception {
        List<List<String>> calls = new ArrayList<>();
        ProcessExecutor executor = new ProcessExecutor() {
            @Override public String run(List<String> command, String message) {
                calls.add(List.copyOf(command));
                throw new ProcessExecutionException("permanent failure", 1, "unavailable");
            }
        };
        String name = "subtitle-test-" + UUID.randomUUID();
        assertThrows(ProcessExecutionException.class, () -> new SubtitleDownloader(executor, "en").download("video", name));
        assertEquals(5, calls.size());
        for (List<String> command : calls) {
            assertEquals("vtt", command.get(command.indexOf("--sub-format") + 1));
            assertTrue(command.contains("--write-subs"));
            assertTrue(command.contains("--write-auto-sub"));
        }
    }

    @Test
    void findsSubtitlesInsideDownloadDirectory(@TempDir Path directory) throws Exception {
        Path basename = directory.resolve("title");
        Path expected = Path.of(basename + ".en.vtt");
        var executor = new ProcessExecutor() {
            @Override public String run(List<String> command, String message) throws IOException {
                Files.writeString(expected, "WEBVTT");
                return "";
            }
        };
        assertEquals(expected, new SubtitleDownloader(executor, "en").download("video", basename.toString()));
    }

    @Test
    void explicitlyRejectsUnsupportedFormat() throws Exception {
        String name = "subtitle-test-" + UUID.randomUUID();
        Path json = Path.of(name + ".en.json3");
        ProcessExecutor executor = new ProcessExecutor() {
            @Override public String run(List<String> command, String message) throws IOException {
                Files.writeString(json, "{}");
                return "";
            }
        };
        try {
            IOException error = assertThrows(IOException.class,
                    () -> new SubtitleDownloader(executor, "en").download("video", name));
            assertTrue(error.getMessage().contains("No supported VTT subtitles"));
        } finally {
            Files.deleteIfExists(json);
        }
    }

    @Test
    void findsRequestedLanguageAndIgnoresSimilarBasenames() throws Exception {
        String name = "subtitle-test-" + UUID.randomUUID();
        Path requested = Path.of(name + ".en.vtt");
        Path wrongLanguage = Path.of(name + ".ru.vtt");
        Path similar = Path.of(name + "-other.en.vtt");
        ProcessExecutor executor = new ProcessExecutor() {
            @Override public String run(List<String> command, String message) throws IOException {
                Files.writeString(requested, "WEBVTT");
                Files.writeString(wrongLanguage, "WEBVTT");
                Files.writeString(similar, "WEBVTT");
                return "";
            }
        };
        try {
            Path result = new SubtitleDownloader(executor, "en").download("video", name);
            assertEquals(requested.toAbsolutePath().normalize(), result.toAbsolutePath().normalize());
        } finally {
            Files.deleteIfExists(requested);
            Files.deleteIfExists(wrongLanguage);
            Files.deleteIfExists(similar);
        }
    }
}
