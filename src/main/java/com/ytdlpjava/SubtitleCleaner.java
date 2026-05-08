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
            "э-э", "а-а", "эм", "ам", "ээ", "ну", "так скажем", "как бы", "вот", "значит", "собственно", "в общем", "э", "а", "да"
    };
    
    private static final String[] STOP_WORDS = {
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
            "видим", "видите", "знаем", "знаете", "понимаем", "понимаете", "например", "вообще", "наверное", "возможно", "конечно",
            "далее", "будем", "тоже", "ещё", "еще", "надо", "поэтому", "этой", "этого", "этим", "этом", "которых", "которые", "какая",
            "какой", "такой", "такие", "свои", "своих", "будет", "быть", "может", "могут", "видеть", "видят", "хотят", "хочет",
            "тебе", "тебя", "себя", "себе", "свое", "своё", "свой", "своя", "свои", "нас", "нам", "ими", "всем", "всех", "всеми",
            "нет", "где", "куда", "откуда", "почему", "зачем", "дальше", "раньше", "позже", "через", "перед", "сзади", "очень",
            "лишь", "разве", "неужели", "бывает", "стало", "стать", "нужно", "можно", "надо", "делает", "делают", "делать",
            "кажется", "значит", "вообще", "именно", "вместе", "однако", "снова", "опять", "прямо", "совсем", "только", "почти",
            "разве", "чтобы", "будто", "словно", "хотя", "если", "пока", "когда", "потому", "поэтому", "значит", "через",
            "наверное", "возможно", "конечно", "надо", "будет", "было", "был", "была", "были", "быть", "есть", "нет",
            "этого", "этому", "этим", "этом", "этой", "эту", "теми", "тех", "том", "тем", "тому", "того", "той", "этими", "эти", "эта", "этот"
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
        Map<String, List<String>> stemmedToOriginals = new HashMap<>();
        Map<String, Integer> stemCounts = new HashMap<>();
        
        Matcher m = Pattern.compile("(?iu)[а-яёa-z]{3,}").matcher(text);
        Set<String> ignoreList = new HashSet<>(Arrays.asList(STOP_WORDS));
        
        while (m.find()) {
            String original = m.group().toLowerCase();
            if (!ignoreList.contains(original)) {
                String stem = stemRussian(original);
                stemCounts.put(stem, stemCounts.getOrDefault(stem, 0) + 1);
                stemmedToOriginals.computeIfAbsent(stem, k -> new ArrayList<>()).add(original);
            }
        }
        
        List<Map.Entry<String, Integer>> topStems = stemCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(30)
                .collect(Collectors.toList());
                
        if (topStems.isEmpty()) return text;
        
        // Map stem back to the most frequent original word form
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
                // Find all unique variations in text that match this stem
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

    private String stemRussian(String word) {
        // Very basic Russian stemming (stripping common endings)
        if (word.length() < 4) return word;
        
        String stem = word;
        // Common endings for cases/plurals
        stem = stem.replaceAll("(иями|ями|ами|ией|ию|ия|ие|ии|ей|ой|ам|ом|а|я|о|е|ы|и|ь)$", "");
        // Adjective endings
        stem = stem.replaceAll("(ому|ему|ого|его|ыми|ими|ых|их|ую|юю|ая|яя|ое|ее|ый|ий|ой|ей)$", "");
        // Verb endings (very basic)
        stem = stem.replaceAll("(ешь|ет|ем|ете|ут|ют|ишь|ит|им|ите|ат|ят|л|ла|ло|ли|ть|ти)$", "");
        
        return stem;
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
        
        // Fix dots after short prepositions (e.g., "с. Победой" -> "с Победой")
        result = result.replaceAll("(?iu)(^|\\s)(в|на|с|из|к|по|о|у|а|и)\\s*\\.\\s*", "$1$2 ");
        
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
            boolean startsWithUpper = line.length() > 0 && Character.isUpperCase(line.charAt(0));
            line = capitalizeAfterSplit(line, startsWithUpper);

            processed.add(wrapText(line, 95));
        }

        return String.join("\n", processed).trim();
    }

    private String capitalizeAfterSplit(String text, boolean startsWithUpper) {
        if (text == null || text.isEmpty()) return text;
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
