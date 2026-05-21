package com.ytdlpjava.processor;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KeywordHighlighter {
    public String highlight(String text, KeywordExtractor.KeywordResult analysis) {
        List<Map.Entry<String, Integer>> topStems = analysis.stemCounts().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(30)
                .collect(Collectors.toList());
                
        if (topStems.isEmpty()) return text;
        
        Map<String, String> stemToDisplayWord = new HashMap<>();
        for (Map.Entry<String, Integer> entry : topStems) {
            String stem = entry.getKey();
            List<String> originals = analysis.stemmedToOriginals().get(stem);
            String mostFrequentOriginal = originals.stream()
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(stem);
            stemToDisplayWord.put(stem, mostFrequentOriginal);
        }
        
        List<String> wordsToHighlight = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : topStems) {
            if (entry.getValue() >= 2) {
                String stem = entry.getKey();
                wordsToHighlight.addAll(new HashSet<>(analysis.stemmedToOriginals().get(stem)));
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
