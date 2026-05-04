package com.ytdlpjava;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class SubtitleCleanerTest {
    @Test
    void testCleanVttToText(@TempDir Path tempDir) throws IOException {
        SubtitleCleaner cleaner = new SubtitleCleaner(2);
        Path vttPath = tempDir.resolve("test.vtt");
        String vttContent = "WEBVTT\n" +
                "\n" +
                "00:00:01.000 --> 00:00:04.000\n" +
                "<c.yellow>Hello</c> [Music] world!\n" +
                "\n" +
                "00:00:05.000 --> 00:00:08.000\n" +
                "This is a <b>test</b>.\n";
        Files.writeString(vttPath, vttContent);

        String result = cleaner.process(vttPath);
        
        assertTrue(result.contains("[00:00:01]"));
        assertTrue(result.contains("Hello world!"));
        assertTrue(result.contains("[00:00:05]"));
        assertTrue(result.contains("This is a test."));
        assertFalse(result.contains("<c.yellow>"));
        assertFalse(result.contains("[Music]"));
    }
}
