package com.ytdlpjava.subtitle.processor;

import com.ytdlpjava.subtitle.config.SubtitleConfig;
import com.ytdlpjava.subtitle.model.SubtitleBlock;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SubtitleTextAssemblerTest {
    private List<String> paragraphs(String text) {
        SubtitleTextAssembler assembler = new SubtitleTextAssembler(new SubtitleConfig(180, 30, 50), List::of);
        return assembler.process(List.of(new SubtitleBlock(Duration.ZERO, text))).stream()
                .filter(item -> !item.startsWith("### [")).toList();
    }

    @Test
    void splitsTextWithoutPunctuationAtWordBoundariesWithoutLosingWords() {
        String text = "word ".repeat(100).trim();
        List<String> paragraphs = paragraphs(text);
        assertTrue(paragraphs.size() > 1);
        assertTrue(paragraphs.stream().allMatch(p -> p.length() <= 50));
        assertEquals(text, String.join(" ", paragraphs));
    }

    @Test
    void splitsSingleLongTokenWithoutLosingCharacters() {
        String text = "x".repeat(201);
        List<String> paragraphs = paragraphs(text);
        assertTrue(paragraphs.stream().allMatch(p -> p.length() <= 50));
        assertEquals(text, String.join("", paragraphs));
    }

    @Test
    void doesNotSplitSurrogatePairs() {
        String text = "x" + "😀".repeat(100);
        List<String> paragraphs = paragraphs(text);
        assertEquals(text, String.join("", paragraphs));
        for (String p : paragraphs) {
            assertTrue(p.length() <= 50);
            assertFalse(Character.isHighSurrogate(p.charAt(p.length() - 1)));
            assertFalse(Character.isLowSurrogate(p.charAt(0)));
        }
    }

    @Test
    void enforcesLimitAcrossSeveralBlocksAndRetainsTimestamps() {
        SubtitleTextAssembler assembler = new SubtitleTextAssembler(new SubtitleConfig(180, 30, 50), List::of);
        List<String> items = assembler.process(List.of(
                new SubtitleBlock(Duration.ZERO, "a".repeat(30)),
                new SubtitleBlock(Duration.ofSeconds(1), "b".repeat(30)),
                new SubtitleBlock(Duration.ofSeconds(200), "c".repeat(30))));
        assertEquals("### [00:00:00]", items.getFirst());
        assertTrue(items.contains("### [00:03:20]"));
        List<String> paragraphs = items.stream().filter(i -> !i.startsWith("### [")).toList();
        assertTrue(paragraphs.stream().allMatch(p -> p.length() <= 50));
        assertEquals("a".repeat(30) + " " + "b".repeat(30) + " " + "c".repeat(30), String.join(" ", paragraphs));
    }

    @Test
    void rejectsInvalidThresholds() {
        assertThrows(IllegalArgumentException.class, () -> new SubtitleConfig(0, 30, 50));
        assertThrows(IllegalArgumentException.class, () -> new SubtitleConfig(180, 0, 50));
        assertThrows(IllegalArgumentException.class, () -> new SubtitleConfig(180, 60, 50));
    }
}
