package com.ytdlpjava.processor;

import com.ytdlpjava.model.TextProcessor;
import com.ytdlpjava.subtitle.api.SubtitleProcessor;
import com.ytdlpjava.subtitle.model.SubtitleBlock;
import com.ytdlpjava.subtitle.processor.SubtitleParser;
import com.ytdlpjava.util.LemmatizerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SubtitleCleaner implements TextProcessor {
    private final LemmatizerService.Language language;
    private final SubtitleParser parser;
    private final SubtitleProcessor subtitleProcessor;
    private final KeywordExtractor extractor;
    private final KeywordHighlighter highlighter;
    private final MarkdownFormatter formatter;

    @Override
    public String processText(String input) {
        List<SubtitleBlock> blocks = parser.parse(input);
        List<String> items = subtitleProcessor.process(blocks);

        String mainText = formatter.format(items);
        KeywordExtractor.KeywordResult analysis = extractor.extract(mainText, language);
        return highlighter.highlight(mainText, analysis);
    }
}
