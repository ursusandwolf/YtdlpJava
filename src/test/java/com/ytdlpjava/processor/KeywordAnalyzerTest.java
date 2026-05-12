package com.ytdlpjava.processor;

import com.ytdlpjava.util.LemmatizerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class KeywordAnalyzerTest {

    private KeywordExtractor extractor;
    private KeywordHighlighter highlighter;
    private LemmatizerService lemmatizer;

    @BeforeEach
    void setUp() {
        lemmatizer = new LemmatizerService();
        List<String> stopWords = Arrays.asList("это", "быть", "я");
        extractor = new KeywordExtractor(stopWords, lemmatizer, LemmatizerService.Language.RU);
        highlighter = new KeywordHighlighter();
    }

    @Test
    void testFilterAndLemmatize() {
        // Добавили слово "говорю" трижды, чтобы trigger'ить подсветку (>= 2 вхождений)
        String text = "Я говорю, я говорю, я говорю, что это было хорошо. Говорили города.";
        KeywordExtractor.KeywordResult analysis = extractor.extract(text, LemmatizerService.Language.RU);
        String result = highlighter.highlight(text, analysis);
        
        System.out.println("DEBUG: Result = " + result);
        
        // Стоп-слова НЕ должны быть выделены
        assertFalse(result.contains("**я**"), "Stop-word 'я' should not be highlighted");
        assertFalse(result.contains("**это**"), "Stop-word 'это' should not be highlighted");
        
        // "говор" -> должен быть выделен как **говорю** (у нас 3 вхождения, >=2)
        assertTrue(result.contains("**говорю**") || result.contains("**говорили**"), "Keywords should be highlighted");
    }
}
