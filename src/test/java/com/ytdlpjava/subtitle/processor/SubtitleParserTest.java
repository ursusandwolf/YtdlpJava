package com.ytdlpjava.subtitle.processor;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class SubtitleParserTest {
    @ParameterizedTest
    @ValueSource(strings = {"\n", "\r\n", "\r"})
    void preservesBlocksAcrossLineEndings(String newline) {
        String vtt = String.join(newline, "WEBVTT", "",
                "00:00:01.000 --> 00:00:02.000", "First", "",
                "00:00:03.000 --> 00:00:04.000", "Second", "");
        var blocks = new SubtitleParser().parse(vtt);
        assertEquals(2, blocks.size());
        assertEquals(Duration.ofSeconds(1), blocks.getFirst().timestamp());
        assertEquals("First", blocks.getFirst().text());
        assertEquals(Duration.ofSeconds(3), blocks.getLast().timestamp());
        assertEquals("Second", blocks.getLast().text().trim());
    }

    @ParameterizedTest
    @ValueSource(strings = {"00:01.500", "00:00:01.500"})
    void acceptsTimestampsWithOptionalHours(String timestamp) {
        var blocks = new SubtitleParser().parse("WEBVTT\n\n" + timestamp + " --> 00:02.000\nText\n");
        assertEquals(1, blocks.size());
        assertEquals(Duration.ofMillis(1500), blocks.getFirst().timestamp());
    }
}
