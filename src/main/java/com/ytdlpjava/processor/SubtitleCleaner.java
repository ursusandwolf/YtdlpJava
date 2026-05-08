package com.ytdlpjava.processor;

import com.ytdlpjava.model.ContentProcessor;
import com.ytdlpjava.util.DictionaryLoader;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SubtitleCleaner implements ContentProcessor {
    private final int minTimestampGapSeconds;
    private final String lang;
    private final SubtitleParser parser = new SubtitleParser();
    private final SubtitleCleanerService cleanerService;
    private final KeywordAnalyzer analyzer;
    private final MarkdownFormatter formatter = new MarkdownFormatter();

    public SubtitleCleaner(int minTimestampGapSeconds, String lang) {
        this.minTimestampGapSeconds = minTimestampGapSeconds;
        this.lang = lang != null ? lang.toLowerCase() : "en";
        
        this.cleanerService = new SubtitleCleanerService(
                DictionaryLoader.load("/dictionaries/fillers.txt"));
        
        // Load combined stop words (generic + language specific)
        List<String> stopWords = new ArrayList<>();
        stopWords.addAll(DictionaryLoader.load("/dictionaries/stop_words_ru.txt"));
        if ("en".equals(this.lang)) {
            stopWords.addAll(DictionaryLoader.load("/dictionaries/stop_words_en.txt"));
        }
        
        this.analyzer = new KeywordAnalyzer(stopWords);
    }

    @Override
    public String process(Path vttPath) throws IOException {
        log.info("Cleaning subtitles ({}): {}", lang, vttPath.getFileName());
        String rawText = Files.readString(vttPath, StandardCharsets.UTF_8);
        
        List<SubtitleParser.SubtitleBlock> blocks = parser.parse(rawText);
        List<String> items = new ArrayList<>();
        
        Duration lastTimestampHeader = null;
        StringBuilder currentParagraph = new StringBuilder();
        String lastAddedLine = null;

        for (SubtitleParser.SubtitleBlock block : blocks) {
            List<String> lines = cleanerService.cleanTextBlock(block.text());
            if (lines.isEmpty()) continue;

            Duration timestamp = block.timestamp();
            
            if (lastTimestampHeader == null || timestamp.minus(lastTimestampHeader).getSeconds() >= minTimestampGapSeconds) {
                if (currentParagraph.length() > 0) {
                    items.add(currentParagraph.toString().trim());
                    currentParagraph.setLength(0);
                }
                items.add("### [" + formatTimestamp(timestamp) + "]");
                lastTimestampHeader = timestamp;
            } else if (currentParagraph.length() > 600) {
                if (isSentenceEnding(currentParagraph.charAt(currentParagraph.length() - 1))) {
                    items.add(currentParagraph.toString().trim());
                    currentParagraph.setLength(0);
                } else if (currentParagraph.length() > 800) {
                    items.add(currentParagraph.toString().trim());
                    currentParagraph.setLength(0);
                }
            }

            for (String line : lines) {
                if (lastAddedLine == null || !line.equals(lastAddedLine)) {
                    if (currentParagraph.length() > 0 && !currentParagraph.toString().endsWith("\n")) {
                        currentParagraph.append(" ");
                    }
                    currentParagraph.append(line);
                    lastAddedLine = line;
                }
            }
        }

        if (currentParagraph.length() > 0) {
            items.add(currentParagraph.toString().trim());
        }

        String mainText = formatter.format(items);
        return analyzer.analyzeAndHighlight(mainText);
    }

    private String formatTimestamp(Duration d) {
        long s = d.getSeconds();
        return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }

    private boolean isSentenceEnding(char c) {
        return c == '.' || c == '!' || c == '?' || c == '…';
    }
}
