package com.ytdlpjava.infrastructure.ytdlp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YtdlpOutputParserTest {
    private final YtdlpOutputParser parser = new YtdlpOutputParser();

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
