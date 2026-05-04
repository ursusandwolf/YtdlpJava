package com.ytdlpjava;

import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProcessExecutor {
    private static final int DEFAULT_TIMEOUT_MINUTES = 30;

    public String run(List<String> command, String errorMessage) throws IOException, InterruptedException {
        return run(command, errorMessage, DEFAULT_TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }

    public String run(List<String> command, String errorMessage, long timeout, TimeUnit unit) throws IOException, InterruptedException {
        log.debug("Executing command: {}", String.join(" ", command));
        
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.trace("Process output: {}", line);
                output.append(line).append("\n");
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

        return output.toString().trim();
    }
}
