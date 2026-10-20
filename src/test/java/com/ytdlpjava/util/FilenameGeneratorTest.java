package com.ytdlpjava.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class FilenameGeneratorTest {
    private final FilenameGenerator generator = new FilenameGenerator();

    @ParameterizedTest
    @CsvSource({"., video", ".., video", "..., video", "CON, _CON", "con.txt, _con.txt",
            "NUL, _NUL", "PRN, _PRN", "AUX, _AUX", "COM1, _COM1", "LPT9.log, _LPT9.log",
            "normal., normal", "CON ., _CON", "COM10, COM10", "COM¹, _COM¹", "LPT².txt, _LPT².txt"})
    void generatesPortableNames(String title, String expected) {
        assertEquals(expected, generator.buildFilename(title, 60));
    }

    @Test
    void keepsNameSafeAfterTruncation() {
        assertEquals("_CO", generator.buildFilename("CONference", 3));
        assertEquals("_CON", generator.buildFilename("CON.txt", 5));
    }

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

    @Test
    void testDeduplicateRepeatingTitles() {
        String title = "929 Книга Пророка Йоэля 929 Книга Пророка Йоэля 929 Книга";
        String result = generator.buildFilename(title, 100);
        assertEquals("929 Книга Пророка Йоэля", result);
    }

    @Test
    void testDeduplicateNestedRepeatingTitles() {
        String title = "重复 重复 重复 Content";
        String result = generator.buildFilename(title, 100);
        assertEquals("重复 Content", result);
    }
}
