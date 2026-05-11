package com.ytdlpjava.core;

import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProcessExecutor {
    private static final int DEFAULT_TIMEOUT_MINUTES = 30;

    public String run(List<String> command, String errorMessage) throws IOException, InterruptedException {
        return run(command, errorMessage, DEFAULT_TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }

    public String run(List<String> command, String errorMessage, long timeout, TimeUnit unit) throws IOException, InterruptedException {
        List<String> finalCommand = new ArrayList<>(command);

        log.debug("Executing command: {}", String.join(" ", finalCommand));
        
        Process process = new ProcessBuilder(finalCommand)
                .redirectErrorStream(true)
                .start();

        String resultPath = null;
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    log.info(line); // Real-time progress logging
                    output.append(line).append("\n");
                    
                    // Path extraction: look for absolute paths or existing files
                    String trimmedLine = line.trim();
                    try {
                        java.nio.file.Path possiblePath = java.nio.file.Path.of(trimmedLine);
                        if (!trimmedLine.startsWith("[") && java.nio.file.Files.exists(possiblePath) && java.nio.file.Files.isRegularFile(possiblePath)) {
                            resultPath = trimmedLine;
                        }
                    } catch (java.nio.file.InvalidPathException | SecurityException ignored) {
                        // Ignore invalid paths
                    }
                }
            }
        }

        boolean finished = process.waitFor(timeout, unit);
        if (!finished) {
            process.destroyForcibly();
            throw new ProcessTimeoutException(errorMessage, timeout, unit);
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            log.error("Command failed with exit code {}. Output: {}", exitCode, output);
            throw new ProcessExecutionException(errorMessage, exitCode, output.toString());
        }

        return resultPath != null ? resultPath : output.toString().trim();
    }
}
