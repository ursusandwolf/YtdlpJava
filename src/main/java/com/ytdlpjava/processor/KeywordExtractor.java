package com.ytdlpjava.processor;

import com.ytdlpjava.util.LemmatizerService;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class KeywordExtractor {
    private final Set<String> stopWords;
    private final LemmatizerService lemmatizer;

    public KeywordExtractor(List<String> stopWords, LemmatizerService lemmatizer, LemmatizerService.Language lang) {
        this.lemmatizer = lemmatizer;
        this.stopWords = stopWords.stream()
                .map(word -> lemmatizer.getLemma(word.toLowerCase(), lang))
                .collect(Collectors.toSet());
    }

    public record KeywordResult(Map<String, Integer> stemCounts, Map<String, List<String>> stemmedToOriginals) {}

    public KeywordResult extract(String text, LemmatizerService.Language lang) {
        Map<String, List<String>> stemmedToOriginals = new HashMap<>();
        Map<String, Integer> stemCounts = new HashMap<>();
        
        Matcher m = Pattern.compile("(?iu)[а-яёa-z]{3,}").matcher(text);
        
        while (m.find()) {
            String original = m.group().toLowerCase();
            String stem = lemmatizer.getLemma(original, lang);
            
            if (!stopWords.contains(stem)) {
                stemCounts.put(stem, stemCounts.getOrDefault(stem, 0) + 1);
                stemmedToOriginals.computeIfAbsent(stem, k -> new ArrayList<>()).add(original);
            }
        }
        return new KeywordResult(stemCounts, stemmedToOriginals);
    }
}
