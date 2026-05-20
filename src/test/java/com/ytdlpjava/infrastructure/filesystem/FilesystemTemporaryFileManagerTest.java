package com.ytdlpjava.infrastructure.filesystem;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesystemTemporaryFileManagerTest {
    private final FilesystemTemporaryFileManager temporaryFileManager = new FilesystemTemporaryFileManager();

    @Test
    void createsTempFileWhenPrefixIsShort() throws Exception {
        Path path = temporaryFileManager.createTempFile("a", ".tmp");

        try {
            assertTrue(Files.exists(path));
            assertTrue(path.getFileName().toString().startsWith("a__"));
        } finally {
            temporaryFileManager.deleteIfExists(path);
        }
    }

    @Test
    void writesContentToTempFile() throws Exception {
        Path path = temporaryFileManager.writeTempFile("subtitles", ".md", "hello");

        try {
            assertEquals("hello", Files.readString(path));
        } finally {
            temporaryFileManager.deleteIfExists(path);
        }
    }

    @Test
    void deletesExistingFileAndIgnoresNull() throws Exception {
        Path path = temporaryFileManager.createTempFile("del", ".tmp");

        temporaryFileManager.deleteIfExists(path);
        temporaryFileManager.deleteIfExists(null);

        assertFalse(Files.exists(path));
    }
}
