package com.ytdlpjava.processor;

import com.ytdlpjava.model.ContentProcessor;
import com.ytdlpjava.model.TextProcessor;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class SubtitleFileProcessor implements ContentProcessor {
    private final TextProcessor textProcessor;

    @Override
    public String process(Path inputPath) throws IOException {
        return textProcessor.processText(Files.readString(inputPath, StandardCharsets.UTF_8));
    }
}
