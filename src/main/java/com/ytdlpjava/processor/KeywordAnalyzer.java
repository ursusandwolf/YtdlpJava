package com.ytdlpjava.processor;

import com.ytdlpjava.util.LemmatizerService;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class KeywordAnalyzer {
    private final Set<String> stopWords;
    private final LemmatizerService lemmatizer;

    public KeywordAnalyzer(List<String> stopWords, LemmatizerService lemmatizer, LemmatizerService.Language lang) {
        this.lemmatizer = lemmatizer;
        this.stopWords = stopWords.stream()
                .map(word -> lemmatizer.getLemma(word.toLowerCase(), lang))
                .collect(Collectors.toSet());
    }

    public String analyzeAndHighlight(String text, LemmatizerService.Language lang) {
        Map<String, List<String>> stemmedToOriginals = new HashMap<>();
        Map<String, Integer> stemCounts = new HashMap<>();
        
        Matcher m = Pattern.compile("(?iu)[а-яёa-z]{3,}").matcher(text);
        
        while (m.find()) {
            String original = m.group().toLowerCase();
            String stem = lemmatizer.getLemma(original, lang);
            
            if (!stopWords.contains(stem)) {
                log.debug("Keeping word={}, stem={}", original, stem);
                stemCounts.put(stem, stemCounts.getOrDefault(stem, 0) + 1);
                stemmedToOriginals.computeIfAbsent(stem, k -> new ArrayList<>()).add(original);
            } else {
                log.debug("Filtering out word={}, stem={}", original, stem);
            }
        }
        
        List<Map.Entry<String, Integer>> topStems = stemCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(30)
                .collect(Collectors.toList());
                
        if (topStems.isEmpty()) return text;
        
        Map<String, String> stemToDisplayWord = new HashMap<>();
        for (Map.Entry<String, Integer> entry : topStems) {
            String stem = entry.getKey();
            List<String> originals = stemmedToOriginals.get(stem);
            String mostFrequentOriginal = originals.stream()
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .get().getKey();
            stemToDisplayWord.put(stem, mostFrequentOriginal);
        }
        
        List<String> wordsToHighlight = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : topStems) {
            if (entry.getValue() >= 2) {
                String stem = entry.getKey();
                wordsToHighlight.addAll(new HashSet<>(stemmedToOriginals.get(stem)));
            }
        }
        
        String highlightedText = text;
        if (!wordsToHighlight.isEmpty()) {
            String combinedPattern = wordsToHighlight.stream()
                    .map(Pattern::quote)
                    .collect(Collectors.joining("|"));
            String pattern = "(?iu)(?<!\\*\\*)\\b(" + combinedPattern + ")\\b(?!\\*\\*)";
            highlightedText = highlightedText.replaceAll(pattern, "**$1**");
        }
        
        StringBuilder result = new StringBuilder(highlightedText);
        result.append("\n\n---\n### Ключевые слова:\n");
        for (Map.Entry<String, Integer> entry : topStems) {
            result.append("- **").append(stemToDisplayWord.get(entry.getKey())).append("**: ").append(entry.getValue()).append("\n");
        }
        
        return result.toString();
    }
}
