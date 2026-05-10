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
        // Add JS runtime if it's a yt-dlp command and not already present
        if (!finalCommand.isEmpty() && "yt-dlp".equals(finalCommand.get(0)) && !finalCommand.contains("--js-runtimes")) {
            finalCommand.add(1, "--js-runtimes");
            finalCommand.add(2, "node");
        }

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
                    
                    // Path extraction: look for absolute paths or specific yt-dlp patterns
                    String trimmedLine = line.trim();
                    if (trimmedLine.startsWith("/") || (trimmedLine.length() > 2 && trimmedLine.charAt(1) == ':')) {
                        resultPath = trimmedLine;
                    }
                }
            }
        }

        boolean finished = process.waitFor(timeout, unit);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException(errorMessage + " (Timeout after " + timeout + " " + unit + ")");
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            log.error("Command failed with exit code {}. Output: {}", exitCode, output);
            throw new RuntimeException("%s (Exit code: %d)".formatted(errorMessage, exitCode));
        }

        return resultPath != null ? resultPath : output.toString().trim();
    }
}
