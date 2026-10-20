package com.ytdlpjava.release;

import com.ytdlpjava.util.DictionaryLoader;
import com.ytdlpjava.util.LemmatizerService;

/** Launched with only test classes and the release JAR on the classpath. */
public class PackagedResourcesProbe {
    public static void main(String[] args) {
        try (var service = new LemmatizerService()) {
            for (var language : LemmatizerService.Language.values()) {
                String code = language.name().toLowerCase(java.util.Locale.ROOT);
                if (DictionaryLoader.load("/dictionaries/stop_words_" + code + ".txt").isEmpty()) {
                    throw new IllegalStateException("Missing packaged dictionary: " + code);
                }
                if (service.getLemma("Україна testing Größe", language).isBlank()) {
                    throw new IllegalStateException("Packaged analyzer failed: " + code);
                }
            }
        }
        System.out.println("PACKAGED_RESOURCES_OK");
    }
}
