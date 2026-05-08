package com.ytdlpjava.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FilenameGeneratorTest {
    private final FilenameGenerator generator = new FilenameGenerator();

    @Test
    void testBuildFilenameWithDate() {
        String title = "Awesome Video 15.03.2026";
        String result = generator.buildFilename(title, 80);
        assertEquals("Awesome Video 2026-03-15", result);
    }

    @Test
    void testBuildFilenameWithEnglishDate() {
        String title = "Interesting Talk 15 Jan 2026";
        String result = generator.buildFilename(title, 80);
        assertEquals("Interesting Talk 2026-01-15", result);
    }

    @Test
    void testSanitizeFilename() {
        String title = "Video: What? <Cool> | File";
        String result = generator.buildFilename(title, 80);
        assertEquals("Video What Cool File", result);
    }

    @Test
    void testSmartTruncate() {
        String title = "This is a very long title that should be truncated properly without cutting words in half";
        String result = generator.buildFilename(title, 20);
        assertTrue(result.length() <= 20);
        assertEquals("This is a very long", result);
    }
}
