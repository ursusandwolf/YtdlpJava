package com.ytdlpjava.util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class LemmatizerService {

    public enum Language { RU, EN }

    private final Map<Language, Analyzer> analyzers = new EnumMap<>(Language.class);

    public LemmatizerService() {
        analyzers.put(Language.RU, new RussianAnalyzer());
        analyzers.put(Language.EN, new EnglishAnalyzer());
    }

    public String getLemma(String word, Language lang) {
        Analyzer analyzer = analyzers.get(lang);
        if (analyzer == null) return word.toLowerCase();

        try (TokenStream ts = analyzer.tokenStream("", word)) {
            CharTermAttribute attr = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            if (ts.incrementToken()) {
                return attr.toString();
            }
        } catch (IOException e) {
            // Fallback
        }
        return word.toLowerCase();
    }
}
