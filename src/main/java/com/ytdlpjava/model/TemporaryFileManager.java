package com.ytdlpjava.model;

import java.io.IOException;
import java.nio.file.Path;

public interface TemporaryFileManager {
    Path createTempFile(String prefix, String suffix) throws IOException;

    Path writeTempFile(String prefix, String suffix, String content) throws IOException;

    void deleteIfExists(Path path) throws IOException;
}
