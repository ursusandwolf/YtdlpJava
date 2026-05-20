package com.ytdlpjava.infrastructure.ytdlp;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;

public class YtdlpOutputParser {
    public Optional<Path> findLastExistingFilePath(String output) {
        if (output == null || output.isBlank()) {
            return Optional.empty();
        }

        Path result = null;
        for (String line : output.lines().map(String::trim).filter(line -> !line.isEmpty()).toList()) {
            if (isPotentialPath(line)) {
                try {
                    Path possiblePath = Path.of(line);
                    if (Files.exists(possiblePath) && Files.isRegularFile(possiblePath)) {
                        result = possiblePath;
                    }
                } catch (InvalidPathException | SecurityException ignored) {
                    // Ignore non-path process output.
                }
            }
        }
        return Optional.ofNullable(result);
    }

    private boolean isPotentialPath(String line) {
        return !line.startsWith("[") && (line.contains("/") || line.contains("\\") || line.contains(":"));
    }
}
