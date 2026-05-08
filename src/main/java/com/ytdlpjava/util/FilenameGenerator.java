package com.ytdlpjava.util;

import com.ytdlpjava.model.FilenameProvider;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FilenameGenerator implements FilenameProvider {
    private static final Pattern ILLEGAL_CHARS = Pattern.compile("[\\\\/*?:\"<>|]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private record DatePattern(Pattern pattern, DateTimeFormatter formatter) {}

    private static final DatePattern[] DATE_PATTERNS = {
            new DatePattern(Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2})\\b"), DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            new DatePattern(Pattern.compile("\\b(\\d{2}\\.\\d{2}\\.\\d{4})\\b"), DateTimeFormatter.ofPattern("dd.MM.yyyy")),
            new DatePattern(Pattern.compile("\\b(\\d{2}/\\d{2}/\\d{4})\\b"), DateTimeFormatter.ofPattern("MM/dd/yyyy")),
            new DatePattern(Pattern.compile("\\b(\\d{4}/\\d{2}/\\d{2})\\b"), DateTimeFormatter.ofPattern("yyyy/MM/dd")),
            new DatePattern(Pattern.compile("\\b(\\d{2}-\\d{2}-\\d{4})\\b"), DateTimeFormatter.ofPattern("MM-dd-yyyy")),
            new DatePattern(Pattern.compile("\\b(\\d{1,2}\\s(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s\\d{4})\\b", Pattern.CASE_INSENSITIVE),
                    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
    };

    @Override
    public String buildFilename(String title, int maxLength) {
        String sanitizedTitle = sanitizeFilename(title);
        var dateInfo = extractDate(sanitizedTitle);

        String titleWoDate = dateInfo.rawDate()
                .map(raw -> sanitizedTitle.replaceAll("\\s*" + Pattern.quote(raw) + "\\s*", " ").trim())
                .orElse(sanitizedTitle);

        int reservedLength = dateInfo.normalizedDate().map(d -> d.length() + 1).orElse(0);
        String mainPart = smartTruncate(titleWoDate, maxLength - reservedLength);

        return dateInfo.normalizedDate()
                .map(d -> (mainPart + " " + d).trim())
                .orElse(mainPart);
    }

    private String sanitizeFilename(String name) {
        name = ILLEGAL_CHARS.matcher(name).replaceAll("");
        String sanitized = WHITESPACE.matcher(name.trim()).replaceAll(" ");
        return sanitized.isEmpty() ? "video" : sanitized;
    }

    private record DateInfo(Optional<String> rawDate, Optional<String> normalizedDate) {}

    private DateInfo extractDate(String text) {
        for (var dp : DATE_PATTERNS) {
            Matcher matcher = dp.pattern().matcher(text);
            if (matcher.find()) {
                String rawDate = matcher.group(1);
                try {
                    LocalDate date = LocalDate.parse(rawDate, dp.formatter());
                    return new DateInfo(Optional.of(rawDate), Optional.of(date.format(DateTimeFormatter.ISO_LOCAL_DATE)));
                } catch (DateTimeParseException ignored) {}
            }
        }
        return new DateInfo(Optional.empty(), Optional.empty());
    }

    private String smartTruncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        String truncated = text.substring(0, maxLength);
        int lastSpace = truncated.lastIndexOf(' ');
        return (lastSpace != -1 ? truncated.substring(0, lastSpace) : truncated).trim();
    }
}
