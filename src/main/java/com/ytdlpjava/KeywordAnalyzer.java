package com.ytdlpjava;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KeywordAnalyzer {
    private final Set<String> stopWords;

    public KeywordAnalyzer(List<String> stopWords) {
        this.stopWords = new HashSet<>(stopWords);
    }

    public String analyzeAndHighlight(String text) {
        Map<String, List<String>> stemmedToOriginals = new HashMap<>();
        Map<String, Integer> stemCounts = new HashMap<>();
        
        Matcher m = Pattern.compile("(?iu)[а-яёa-z]{3,}").matcher(text);
        
        while (m.find()) {
            String original = m.group().toLowerCase();
            if (!stopWords.contains(original)) {
                String stem = stemWord(original);
                stemCounts.put(stem, stemCounts.getOrDefault(stem, 0) + 1);
                stemmedToOriginals.computeIfAbsent(stem, k -> new ArrayList<>()).add(original);
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
        
        String highlightedText = text;
        for (Map.Entry<String, Integer> entry : topStems) {
            if (entry.getValue() >= 2) {
                String stem = entry.getKey();
                Set<String> variations = new HashSet<>(stemmedToOriginals.get(stem));
                for (String word : variations) {
                    String pattern = "(?iu)(?<!\\*\\*)\\b(" + Pattern.quote(word) + ")\\b(?!\\*\\*)";
                    highlightedText = highlightedText.replaceAll(pattern, "**$1**");
                }
            }
        }
        
        StringBuilder result = new StringBuilder(highlightedText);
        result.append("\n\n---\n### Ключевые слова:\n");
        for (Map.Entry<String, Integer> entry : topStems) {
            result.append("- **").append(stemToDisplayWord.get(entry.getKey())).append("**: ").append(entry.getValue()).append("\n");
        }
        
        return result.toString();
    }

    private String stemWord(String word) {
        if (word.length() < 4) return word;
        
        // Russian check
        if (word.matches(".*[а-яё].*")) {
            return stemRussian(word);
        }
        
        // Basic English stemming
        String stem = word;
        if (stem.endsWith("ies") && !stem.endsWith("eies") && !stem.endsWith("aies")) {
            stem = stem.substring(0, stem.length() - 3) + "i";
        } else if (stem.endsWith("es") && (stem.endsWith("ses") || stem.endsWith("xes") || stem.endsWith("ches") || stem.endsWith("shes"))) {
            stem = stem.substring(0, stem.length() - 2);
        } else if (stem.endsWith("s") && !stem.endsWith("ss") && !stem.endsWith("us")) {
            stem = stem.substring(0, stem.length() - 1);
        }
        
        if (stem.endsWith("ing")) {
            stem = stem.substring(0, stem.length() - 3);
        } else if (stem.endsWith("ed")) {
            stem = stem.substring(0, stem.length() - 2);
        }
        
        return stem;
    }

    private String stemRussian(String word) {
        String stem = word;
        stem = stem.replaceAll("(иями|ями|ами|ией|ию|ия|ие|ии|ей|ой|ам|ом|а|я|о|е|ы|и|ь)$", "");
        stem = stem.replaceAll("(ому|ему|ого|его|ыми|ими|ых|их|ую|юю|ая|яя|ое|ее|ый|ий|ой|ей)$", "");
        stem = stem.replaceAll("(ешь|ет|ем|ете|ут|ют|ишь|ит|им|ите|ат|ят|л|ла|ло|ли|ть|ти)$", "");
        return stem;
    }
}
