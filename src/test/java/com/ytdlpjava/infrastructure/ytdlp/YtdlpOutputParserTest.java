package com.ytdlpjava.infrastructure.ytdlp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YtdlpOutputParserTest {
    private final YtdlpOutputParser parser = new YtdlpOutputParser();

    @ParameterizedTest
    @ValueSource(strings = {"plain", "[brackets]"})
    void acceptsExistingFileInCurrentDirectory(String name) throws Exception {
        Path file = Path.of(name + "-" + UUID.randomUUID() + ".mp4");
        Files.writeString(file, "content");
        try {
            assertEquals(file, parser.findLastExistingFilePath("[download] 100%\n" + file + "\n").orElseThrow());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void findsLastExistingFilePath(@TempDir Path tempDir) throws Exception {
        Path firstFile = Files.writeString(tempDir.resolve("first.mp4"), "first");
        Path secondFile = Files.writeString(tempDir.resolve("second.mp4"), "second");
        String output = "[download] 100% of 1MiB\n" +
                firstFile + "\n" +
                "some progress line\n" +
                secondFile + "\n";

        assertEquals(secondFile, parser.findLastExistingFilePath(output).orElseThrow());
    }

    @Test
    void ignoresNonPathOutput() {
        String output = "[download] 100% of 1MiB\n" +
                "not a path\n" +
                "/tmp/file-that-does-not-exist.mp4\n";

        assertTrue(parser.findLastExistingFilePath(output).isEmpty());
    }
}
