package com.ytdlpjava;

import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DictionaryLoader {
    public static List<String> load(String resourcePath) {
        try (InputStream is = DictionaryLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                log.warn("Resource not found: {}", resourcePath);
                return Collections.emptyList();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.error("Failed to load dictionary: {}", resourcePath, e);
            return Collections.emptyList();
        }
    }
}
