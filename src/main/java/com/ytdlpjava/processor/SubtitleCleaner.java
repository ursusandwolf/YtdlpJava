package com.ytdlpjava.processor;

import com.ytdlpjava.model.ContentProcessor;
import com.ytdlpjava.model.TextProcessor;
import com.ytdlpjava.subtitle.config.SubtitleConfig;
import com.ytdlpjava.subtitle.model.SubtitleBlock;
import com.ytdlpjava.subtitle.processor.SubtitleParser;
import com.ytdlpjava.util.LemmatizerService;
import com.ytdlpjava.util.TextFormatUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SubtitleCleaner implements ContentProcessor, TextProcessor {
    private final SubtitleConfig config;
    private final LemmatizerService.Language language;
    private final SubtitleParser parser;
    private final SubtitleCleanerService cleanerService;
    private final KeywordExtractor extractor;
    private final KeywordHighlighter highlighter;
    private final MarkdownFormatter formatter;

    @Override
    public String process(Path vttPath) throws IOException {
        log.info("Cleaning subtitles ({}): {}", language, vttPath.getFileName());
        String rawText = Files.readString(vttPath, StandardCharsets.UTF_8);
        return processText(rawText);
    }

    @Override
    public String processText(String input) {
        List<SubtitleBlock> blocks = parser.parse(input);
        List<String> items = new ArrayList<>();
        
        Duration lastTimestampHeader = null;
        StringBuilder currentParagraph = new StringBuilder();
        String lastAddedLine = null;

        for (SubtitleBlock block : blocks) {
            List<String> lines = cleanerService.cleanTextBlock(block.text());
            if (lines.isEmpty()) continue;

            Duration timestamp = block.timestamp();
            
            if (lastTimestampHeader == null || timestamp.minus(lastTimestampHeader).getSeconds() >= config.minTimestampGapSeconds()) {
                if (currentParagraph.length() > 0) {
                    items.add(currentParagraph.toString().trim());
                    currentParagraph.setLength(0);
                }
                items.add("### [" + TextFormatUtils.formatTimestamp(timestamp) + "]");
                lastTimestampHeader = timestamp;
            } else if (currentParagraph.length() > config.paragraphSoftLimit()) {
                if (TextFormatUtils.isSentenceEnding(currentParagraph.charAt(currentParagraph.length() - 1))) {
                    items.add(currentParagraph.toString().trim());
                    currentParagraph.setLength(0);
                } else if (currentParagraph.length() > config.paragraphHardLimit()) {
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
        KeywordExtractor.KeywordResult analysis = extractor.extract(mainText, language);
        return highlighter.highlight(mainText, analysis);
    }
}
