package com.ytdlpjava.infrastructure.filesystem;

import com.ytdlpjava.model.TemporaryFileManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesystemTemporaryFileManager implements TemporaryFileManager {
    @Override
    public Path createTempFile(String prefix, String suffix) throws IOException {
        return Files.createTempFile(normalizePrefix(prefix), suffix);
    }

    @Override
    public Path writeTempFile(String prefix, String suffix, String content) throws IOException {
        Path path = createTempFile(prefix, suffix);
        Files.writeString(path, content);
        return path;
    }

    @Override
    public void deleteIfExists(Path path) throws IOException {
        if (path != null) {
            Files.deleteIfExists(path);
        }
    }

    private String normalizePrefix(String prefix) {
        String normalized = prefix == null ? "tmp" : prefix;
        if (normalized.length() >= 3) {
            return normalized;
        }
        return (normalized + "___").substring(0, 3);
    }
}
