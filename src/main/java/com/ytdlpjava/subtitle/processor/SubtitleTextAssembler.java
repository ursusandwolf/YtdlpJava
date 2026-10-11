package com.ytdlpjava.subtitle.processor;

import com.ytdlpjava.subtitle.api.SubtitleProcessor;
import com.ytdlpjava.subtitle.config.SubtitleConfig;
import com.ytdlpjava.subtitle.model.SubtitleBlock;
import com.ytdlpjava.util.TextFormatUtils;
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
                items.add("### [" + TextFormatUtils.formatTimestamp(timestamp) + "]");
                lastTimestampHeader = timestamp;
            } else if (currentParagraph.length() > config.paragraphSoftLimit()) {
                if (TextFormatUtils.isSentenceEnding(currentParagraph.charAt(currentParagraph.length() - 1))) {
                    items.add(currentParagraph.toString().trim());
                    currentParagraph.setLength(0);
                }
            }

            for (String line : lines) {
                if (lastAddedLine == null || !line.equals(lastAddedLine)) {
                    if (currentParagraph.length() > 0) {
                        currentParagraph.append(" ");
                    }
                    currentParagraph.append(line);
                    enforceHardLimit(currentParagraph, items);
                    lastAddedLine = line;
                }
            }
        }

        if (currentParagraph.length() > 0) {
            items.add(currentParagraph.toString().trim());
        }
        return items;
    }

    private void enforceHardLimit(StringBuilder paragraph, List<String> items) {
        int limit = config.paragraphHardLimit();
        while (paragraph.length() > limit) {
            int split = limit;
            for (int i = limit; i > 0; i--) {
                if (Character.isWhitespace(paragraph.charAt(i))) {
                    split = i;
                    break;
                }
            }
            // Avoid splitting a UTF-16 surrogate pair in a long token.
            if (split == limit && Character.isHighSurrogate(paragraph.charAt(split - 1))) split--;
            if (split == 0) throw new IllegalArgumentException("Paragraph limit is too small for this text");
            items.add(paragraph.substring(0, split).trim());
            paragraph.delete(0, split);
            while (!paragraph.isEmpty() && Character.isWhitespace(paragraph.charAt(0))) paragraph.deleteCharAt(0);
        }
    }
}
