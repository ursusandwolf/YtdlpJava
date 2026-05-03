package com.ytdlpjava;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubtitleCleaner {
    private static final Pattern BLOCK_PATTERN = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}\\.\\d{3}) --> .*?\\n(.*?)(?=\\n\\n|\\Z)", Pattern.DOTALL);
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]+>");
    private static final Pattern SPEAKER_TAGS = Pattern.compile("\\[.*?\\]");

    public String cleanVttToText(Path vttPath, int minTimestampGapSeconds) throws IOException {
        String rawText = Files.readString(vttPath, StandardCharsets.UTF_8);
        List<String> cleanedLines = new ArrayList<>();
        Duration lastTimestamp = null;

        Matcher matcher = BLOCK_PATTERN.matcher(rawText);
        while (matcher.find()) {
            String timestampStr = matcher.group(1);
            String textBlock = matcher.group(2);

            List<String> lines = cleanTextBlock(textBlock);
            if (lines.isEmpty()) continue;

            Duration timestamp = parseTimestamp(timestampStr);
            if (lastTimestamp == null || timestamp.minus(lastTimestamp).getSeconds() >= minTimestampGapSeconds) {
                cleanedLines.add("\n[" + formatTimestamp(timestamp) + "]");
                lastTimestamp = timestamp;
            }

            for (String line : lines) {
                if (cleanedLines.isEmpty() || !line.equals(cleanedLines.get(cleanedLines.size() - 1))) {
                    cleanedLines.add(line);
                }
            }
        }

        return postProcessText(cleanedLines);
    }

    private List<String> cleanTextBlock(String textBlock) {
        String[] lines = textBlock.strip().split("\\n");
        List<String> cleaned = new ArrayList<>();
        for (String line : lines) {
            line = HTML_TAGS.matcher(line).replaceAll("");
            line = SPEAKER_TAGS.matcher(line).replaceAll("");
            line = line.replaceAll("\\s+", " ");
            line = unescapeHtml(line.trim());
            if (!line.isEmpty()) cleaned.add(line);
        }
        return cleaned;
    }

    private String postProcessText(List<String> lines) {
        List<String> processed = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("\n[")) {
                processed.add(line);
                continue;
            }

            if (i > 0 && !processed.isEmpty()) {
                String last = processed.get(processed.size() - 1);
                if (last.endsWith(".") || last.endsWith("!") || last.endsWith("?") || last.endsWith("…")) {
                    line = line.substring(0, 1).toUpperCase() + line.substring(1);
                }
            }

            if (!line.endsWith(".") && !line.endsWith("!") && !line.endsWith("?") && !line.endsWith("…")) {
                line += ".";
            }
            processed.add(line);
        }

        String text = String.join("\n", processed).trim();
        if (!text.isEmpty()) {
            Matcher m = Pattern.compile("[a-zA-Zа-яА-Я]").matcher(text);
            if (m.find()) {
                int start = m.start();
                text = text.substring(0, start) + text.substring(start, start + 1).toUpperCase() + text.substring(start + 1);
            }
        }
        return text;
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
