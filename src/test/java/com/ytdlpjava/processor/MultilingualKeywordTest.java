package com.ytdlpjava.processor;

import com.ytdlpjava.util.LemmatizerService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class MultilingualKeywordTest {
    @ParameterizedTest
    @CsvSource({"UK, Україна", "UK, Київ", "DE, Größe", "ES, español", "FR, français",
            "FR, école", "IT, città", "EN, apple", "RU, город", "FR, école"})
    void countsAndHighlightsWholeUnicodeWords(LemmatizerService.Language language, String word) {
        try (var service = new LemmatizerService()) {
            var extractor = new KeywordExtractor(List.of(), service, language);
            String text = word + " " + word;
            var analysis = extractor.extract(text, language);
            assertEquals(List.of(word.toLowerCase(Locale.ROOT), word.toLowerCase(Locale.ROOT)),
                    analysis.stemmedToOriginals().values().stream().flatMap(List::stream).toList());
            assertEquals(2, analysis.stemCounts().values().stream().mapToInt(Integer::intValue).sum());
            assertTrue(new KeywordHighlighter().highlight(text, analysis).startsWith("**" + word + "** **" + word + "**"));
        }
    }
}
