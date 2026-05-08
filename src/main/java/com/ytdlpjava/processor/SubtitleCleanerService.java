package com.ytdlpjava.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class SubtitleCleanerService {
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]+>");
    private static final Pattern SPEAKER_TAGS = Pattern.compile("\\[.*?\\]");
    
    private final List<String> fillers;

    public SubtitleCleanerService(List<String> fillers) {
        this.fillers = fillers;
    }

    public List<String> cleanTextBlock(String textBlock) {
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
        for (String filler : fillers) {
            String pattern = "(?iu)(?<=^|[^а-яёa-z])" + Pattern.quote(filler) + "(?=[^а-яёa-z]|$)[,\\s-]*";
            result = result.replaceAll(pattern, " ");
        }
        
        result = result.replaceAll(",\\s*[.,!?…]+", ".");
        result = result.replaceAll("\\s+,", ",");
        result = result.replaceAll(",+", ",");
        result = result.replaceAll("\\.{2,}", ".");
        result = result.replaceAll("(?iu)(^|\\s)(в|на|с|из|к|по|о|у|а|и)\\s*\\.\\s*", "$1$2 ");
        
        return result.replaceAll("\\s+", " ").trim();
    }

    private String unescapeHtml(String s) {
        return s.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'");
    }
}
