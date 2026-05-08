package com.ytdlpjava.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.Duration;

public class SubtitleParser {
    private static final Pattern BLOCK_PATTERN = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}\\.\\d{3}) --> .*?\\n(.*?)(?=\\n\\n|\\Z)", Pattern.DOTALL);

    public record SubtitleBlock(Duration timestamp, String text) {}

    public List<SubtitleBlock> parse(String rawText) {
        List<SubtitleBlock> blocks = new ArrayList<>();
        Matcher matcher = BLOCK_PATTERN.matcher(rawText);
        while (matcher.find()) {
            Duration timestamp = parseTimestamp(matcher.group(1));
            blocks.add(new SubtitleBlock(timestamp, matcher.group(2)));
        }
        return blocks;
    }

    private Duration parseTimestamp(String ts) {
        String[] parts = ts.split(":");
        String[] sMs = parts[2].split("\\.");
        return Duration.ofHours(Long.parseLong(parts[0]))
                .plusMinutes(Long.parseLong(parts[1]))
                .plusSeconds(Long.parseLong(sMs[0]))
                .plusMillis(Long.parseLong(sMs[1]));
    }
}
