package com.ytdlpjava;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class SubtitleCleaner implements ContentProcessor {
    private static final Pattern BLOCK_PATTERN = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}\\.\\d{3}) --> .*?\\n(.*?)(?=\\n\\n|\\Z)", Pattern.DOTALL);
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]+>");
    private static final Pattern SPEAKER_TAGS = Pattern.compile("\\[.*?\\]");
    
    private static final String[] FILLERS = {
            "э-э", "а-а", "ну", "так скажем", "как бы", "вот", "значит", "собственно", "в общем", "э", "а", "да"
    };
    
    private static final String[] PREPOSITIONS = {
            "в", "на", "с", "из", "к", "по", "о", "у", "для", "за", "от", "до", "без", "над", "под", "при", "про", "через",
            "и", "а", "но", "да", "или", "что", "как", "если", "то", "чтобы", "это", "этот", "эта", "эти", "был", "была", "было", "были",
            "мы", "вы", "он", "она", "они", "меня", "вам", "вас", "нами", "вами", "есть", "быть", "если", "уже", "очень", "только", 
            "просто", "какой", "такой", "когда", "потому", "может", "можно", "будет", "было", "быть", "все", "всё", "его", "ее", "её",
            "их", "того", "тому", "этого", "этому", "здесь", "там", "тут", "сейчас", "потом", "тогда", "через", "хотя", "чтобы",
            "даже", "вдруг", "между", "перед", "после", "вдоль", "сквозь", "возле", "около", "вокруг", "напротив", "кроме", "среди",
            "внутри", "вне", "вместо", "согласно", "несмотря", "благодаря", "начиная", "включая", "более", "менее", "всего", "больше",
            "меньше", "лучше", "хуже", "совсем", "совсем", "вместе", "совсем", "почти", "снова", "опять", "так", "как", "какой", "такой",
            "какая", "такая", "какое", "такое", "какие", "такие", "который", "которая", "которое", "которые", "кто", "что", "чей", "чья",
            "чье", "чьи", "сам", "сама", "само", "сами", "весь", "вся", "все", "всё", "много", "мало", "сколько", "столько", "несколько",
            "видим", "видите", "знаем", "знаете", "понимаем", "понимаете", "например", "вообще", "наверное", "возможно", "конечно"
    };

    private final int minTimestampGapSeconds;

    public SubtitleCleaner(int minTimestampGapSeconds) {
        this.minTimestampGapSeconds = minTimestampGapSeconds;
    }

    @Override
    public String process(Path vttPath) throws IOException {
        log.info("Cleaning subtitles: {}", vttPath.getFileName());
        String rawText = Files.readString(vttPath, StandardCharsets.UTF_8);
        List<String> cleanedLines = new ArrayList<>();
        Duration lastTimestamp = null;
        StringBuilder currentTextAccumulator = new StringBuilder();
        String lastAddedLine = null;

        Matcher matcher = BLOCK_PATTERN.matcher(rawText);
        while (matcher.find()) {
            String timestampStr = matcher.group(1);
            String textBlock = matcher.group(2);

            List<String> lines = cleanTextBlock(textBlock);
            if (lines.isEmpty()) continue;

            Duration timestamp = parseTimestamp(timestampStr);
            boolean forceNewBlock = currentTextAccumulator.length() > 600 && isSentenceEnding(currentTextAccumulator.charAt(currentTextAccumulator.length() - 1));
            
            if (lastTimestamp == null || forceNewBlock || timestamp.minus(lastTimestamp).getSeconds() >= minTimestampGapSeconds) {
                if (currentTextAccumulator.length() > 0) {
                    cleanedLines.add(currentTextAccumulator.toString().trim());
                    currentTextAccumulator.setLength(0);
                }
                cleanedLines.add("\n### [" + formatTimestamp(timestamp) + "]");
                lastTimestamp = timestamp;
                // Do NOT reset lastAddedLine to avoid repetition across blocks
            }

            for (String line : lines) {
                if (lastAddedLine == null || !line.equals(lastAddedLine)) {
                    if (currentTextAccumulator.length() > 0 && !currentTextAccumulator.toString().endsWith("\n")) {
                        currentTextAccumulator.append(" ");
                    }
                    currentTextAccumulator.append(line);
                    lastAddedLine = line;
                }
            }
        }

        if (currentTextAccumulator.length() > 0) {
            cleanedLines.add(currentTextAccumulator.toString().trim());
        }

        String mainText = postProcessText(cleanedLines);
        return addKeywordsSummary(mainText);
    }

    private String addKeywordsSummary(String text) {
        Map<String, Integer> wordCounts = new HashMap<>();
        // Simple regex for words, supporting Cyrillic
        Matcher m = Pattern.compile("(?iu)[а-яёa-z]{3,}").matcher(text);
        
        Set<String> ignoreList = new HashSet<>(Arrays.asList(PREPOSITIONS));
        
        while (m.find()) {
            String word = m.group().toLowerCase();
            if (!ignoreList.contains(word)) {
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
            }
        }
        
        List<Map.Entry<String, Integer>> topKeywords = wordCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(30)
                .collect(Collectors.toList());
                
        if (topKeywords.isEmpty()) return text;
        
        String highlightedText = text;
        for (Map.Entry<String, Integer> entry : topKeywords) {
            if (entry.getValue() >= 2) {
                String word = entry.getKey();
                // Bold the word, making sure not to double-bold or bold within bold
                String pattern = "(?iu)(?<!\\*\\*)\\b(" + Pattern.quote(word) + ")\\b(?!\\*\\*)";
                highlightedText = highlightedText.replaceAll(pattern, "**$1**");
            }
        }
        
        StringBuilder result = new StringBuilder(highlightedText);
        result.append("\n\n---\n### Ключевые слова:\n");
        for (Map.Entry<String, Integer> entry : topKeywords) {
            result.append("- **").append(entry.getKey()).append("**: ").append(entry.getValue()).append("\n");
        }
        
        return result.toString();
    }

    private List<String> cleanTextBlock(String textBlock) {
        String[] lines = textBlock.strip().split("\\n");
        List<String> cleaned = new ArrayList<>();
        for (String line : lines) {
            line = HTML_TAGS.matcher(line).replaceAll("");
            line = SPEAKER_TAGS.matcher(line).replaceAll("");
            
            line = smartCleanFillers(line);
            
            line = line.replaceAll("\\s+", " ");
            line = unescapeHtml(line.trim());
            if (!line.isEmpty()) cleaned.add(line);
        }
        return cleaned;
    }

    private String smartCleanFillers(String line) {
        String result = line;
        for (String filler : FILLERS) {
            String pattern = "(?iu)(?<=^|[^а-яёa-z])" + Pattern.quote(filler) + "(?=[^а-яёa-z]|$)[,\\s-]*";
            result = result.replaceAll(pattern, " ");
        }
        
        // Cleanup punctuation mess left by filler removal
        result = result.replaceAll(",\\s*[.,!?…]+", "."); // ",." -> "."
        result = result.replaceAll("\\s+,", ",");       // " ," -> ","
        result = result.replaceAll(",+", ",");          // ",," -> ","
        result = result.replaceAll("\\.{2,}", ".");     // ".." -> "."
        
        return result.replaceAll("\\s+", " ").trim();
    }

    private String postProcessText(List<String> lines) {
        List<String> processed = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("\n### [")) {
                processed.add(line);
                continue;
            }

            if (!line.isEmpty()) {
                boolean shouldCapitalize = true;
                if (i > 1) {
                    String prevText = lines.get(i - 2);
                    if (!prevText.isEmpty() && !isSentenceEnding(prevText.charAt(prevText.length() - 1))) {
                        shouldCapitalize = false;
                    }
                }
                
                if (shouldCapitalize) {
                    line = line.substring(0, 1).toUpperCase() + line.substring(1);
                } else {
                    line = line.substring(0, 1).toLowerCase() + line.substring(1);
                }
            }

            // Split long sentences if they exceed 300 chars
            line = splitLongSentences(line, 300);
            
            // Re-capitalize after forced splits, but respect initial capitalization
            boolean startsWithUpper = Character.isUpperCase(line.charAt(0));
            line = capitalizeAfterSplit(line, startsWithUpper);

            processed.add(wrapText(line, 95));
        }

        return String.join("\n", processed).trim();
    }

    private String capitalizeAfterSplit(String text, boolean startsWithUpper) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = startsWithUpper;
        boolean firstLetterFound = false;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (nextUpper && Character.isLetter(c)) {
                sb.append(Character.toUpperCase(c));
                nextUpper = false;
                firstLetterFound = true;
            } else if (!firstLetterFound && Character.isLetter(c)) {
                // Respect initial lowercase if requested
                sb.append(c);
                firstLetterFound = true;
                nextUpper = false;
            } else {
                sb.append(c);
                if (isSentenceEnding(c)) {
                    nextUpper = true;
                }
            }
        }
        return sb.toString();
    }

    private String splitLongSentences(String text, int maxLength) {
        String[] parts = text.split("(?<=[.!?…])\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.length() > maxLength) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(forceSplitSentence(part, maxLength));
            } else {
                if (sb.length() > 0) sb.append(" ");
                sb.append(part);
            }
        }
        return sb.toString();
    }

    private String forceSplitSentence(String sentence, int maxLength) {
        if (sentence.length() <= maxLength) return sentence;
        
        String searchArea = sentence.substring(0, Math.min(sentence.length(), maxLength));
        int breakPoint = searchArea.lastIndexOf(", ");
        if (breakPoint == -1) breakPoint = searchArea.lastIndexOf("; ");
        if (breakPoint == -1) breakPoint = searchArea.lastIndexOf(" "); 
        
        if (breakPoint != -1) {
            return sentence.substring(0, breakPoint + 1).trim() + ".\n" + 
                   Character.toUpperCase(sentence.charAt(breakPoint + 1)) + 
                   forceSplitSentence(sentence.substring(breakPoint + 2).trim(), maxLength);
        }
        
        return sentence;
    }

    private String wrapText(String text, int maxLength) {
        StringBuilder result = new StringBuilder();
        String[] words = text.split(" ");
        int currentLineLength = 0;

        for (String word : words) {
            // Check if adding the word (plus a space) exceeds maxLength
            // We strip markdown bold markers for length calculation to be more accurate
            int wordLength = word.replaceAll("\\*\\*", "").length();
            
            if (currentLineLength + wordLength + 1 > maxLength) {
                result.append("\n");
                currentLineLength = 0;
            } else if (currentLineLength > 0) {
                result.append(" ");
                currentLineLength++;
            }
            
            result.append(word);
            currentLineLength += wordLength;
        }

        return result.toString();
    }

    private boolean isSentenceEnding(char c) {
        return c == '.' || c == '!' || c == '?' || c == '…';
    }

    private Duration parseTimestamp(String ts) {
        String[] parts = ts.split(":");
        String[] sMs = parts[2].split("\\.");
        return Duration.ofHours(Long.parseLong(parts[0]))
                .plusMinutes(Long.parseLong(parts[1]))
                .plusSeconds(Long.parseLong(sMs[0]))
                .plusMillis(Long.parseLong(sMs[1]));
    }

    private String formatTimestamp(Duration d) {
        long s = d.getSeconds();
        return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }

    private String unescapeHtml(String s) {
        return s.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'");
    }
}
