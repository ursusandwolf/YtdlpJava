package com.ytdlpjava.subtitle.processor;

import com.ytdlpjava.subtitle.api.SubtitleProcessor;
import com.ytdlpjava.subtitle.config.SubtitleConfig;
import com.ytdlpjava.subtitle.model.SubtitleBlock;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class SubtitleTextAssembler implements SubtitleProcessor {
    private final SubtitleConfig config;
    private final SubtitleLineCleaner lineCleaner;

    @Override
    public List<String> process(List<SubtitleBlock> blocks) {
        List<String> items = new ArrayList<>();
        Duration lastTimestampHeader = null;
        StringBuilder currentParagraph = new StringBuilder();
        String lastAddedLine = null;

        for (SubtitleBlock block : blocks) {
            List<String> lines = lineCleaner.clean(block.text());
            if (lines.isEmpty()) continue;

            Duration timestamp = block.timestamp();

            if (lastTimestampHeader == null || timestamp.minus(lastTimestampHeader).getSeconds() >= config.minTimestampGapSeconds()) {
                if (currentParagraph.length() > 0) {
                    items.add(currentParagraph.toString().trim());
                    currentParagraph.setLength(0);
                }
                items.add("### [" + formatTimestamp(timestamp) + "]");
                lastTimestampHeader = timestamp;
            } else if (currentParagraph.length() > config.paragraphLengthLimit()) {
                if (isSentenceEnding(currentParagraph.charAt(currentParagraph.length() - 1))) {
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
        return items;
    }

    private String formatTimestamp(Duration d) {
        long s = d.getSeconds();
        return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }

    private boolean isSentenceEnding(char c) {
        return c == '.' || c == '!' || c == '?' || c == '…';
    }
}
