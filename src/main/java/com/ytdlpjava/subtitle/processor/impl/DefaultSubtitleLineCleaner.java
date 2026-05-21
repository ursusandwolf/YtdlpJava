package com.ytdlpjava.subtitle.processor.impl;

import com.ytdlpjava.subtitle.processor.SubtitleLineCleaner;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DefaultSubtitleLineCleaner implements SubtitleLineCleaner {
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]+>");
    private static final Pattern SPEAKER_TAGS = Pattern.compile("\\[.*?\\]");
    private static final Pattern COMMA_AND_PUNCTUATION = Pattern.compile(",\\s*[.,!?…]+");
    private static final Pattern SPACE_BEFORE_COMMA = Pattern.compile("\\s+,");
    private static final Pattern MULTIPLE_COMMAS = Pattern.compile(",+");
    private static final Pattern MULTIPLE_DOTS = Pattern.compile("\\.{2,}");
    private static final Pattern PREPOSITION_DOT = Pattern.compile("(?iu)(^|\\s)(в|на|с|из|к|по|о|у|а|и)\\s*\\.\\s*");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    
    private final List<Pattern> fillerPatterns;

    public DefaultSubtitleLineCleaner(List<String> fillers) {
        this.fillerPatterns = fillers.stream()
                .map(filler -> Pattern.compile("(?iu)(?<=^|[^а-яёa-z])" + Pattern.quote(filler) + "(?=[^а-яёa-z]|$)[,\\s-]*"))
                .toList();
    }

    @Override
    public List<String> clean(String textBlock) {
        String[] lines = textBlock.strip().split("\\n");
        List<String> cleaned = new ArrayList<>();
        for (String line : lines) {
            line = HTML_TAGS.matcher(line).replaceAll("");
            line = SPEAKER_TAGS.matcher(line).replaceAll("");
            line = smartCleanFillers(line);
            line = MULTIPLE_SPACES.matcher(line).replaceAll(" ");
            line = unescapeHtml(line.trim());
            if (!line.isEmpty()) cleaned.add(line);
        }
        return cleaned;
    }

    private String smartCleanFillers(String line) {
        String result = line;
        for (Pattern pattern : fillerPatterns) {
            result = pattern.matcher(result).replaceAll(" ");
        }
        
        result = COMMA_AND_PUNCTUATION.matcher(result).replaceAll(".");
        result = SPACE_BEFORE_COMMA.matcher(result).replaceAll(",");
        result = MULTIPLE_COMMAS.matcher(result).replaceAll(",");
        result = MULTIPLE_DOTS.matcher(result).replaceAll(".");
        result = PREPOSITION_DOT.matcher(result).replaceAll("$1$2 ");
        
        return MULTIPLE_SPACES.matcher(result).replaceAll(" ").trim();
    }

    private String unescapeHtml(String s) {
        return s.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'");
    }
}
