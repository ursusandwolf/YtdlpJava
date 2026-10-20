package com.ytdlpjava.core;

import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class ProcessExecutor {
    private static final int DEFAULT_TIMEOUT_MINUTES = 30;

    public String run(List<String> command, String errorMessage) throws IOException, InterruptedException {
        return run(command, errorMessage, DEFAULT_TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }

    public String run(List<String> command, String errorMessage, long timeout, TimeUnit unit)
            throws IOException, InterruptedException {
        if (timeout <= 0) throw new IllegalArgumentException("Process timeout must be positive");
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        log.debug("Executing command: {}", String.join(" ", command));
        Process process = new ProcessBuilder(List.copyOf(command)).redirectErrorStream(true).start();
        FutureTask<String> reader = new FutureTask<>(() -> readOutput(process));
        Thread.ofVirtual().name("process-output").start(reader);
        try {
            process.getOutputStream().close();
            if (!process.waitFor(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS)) {
                throw new ProcessTimeoutException(errorMessage, timeout, unit);
            }
            String output;
            try {
                output = reader.get(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS);
            } catch (TimeoutException e) {
                throw new ProcessTimeoutException(errorMessage, timeout, unit);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof IOException io) throw io;
                throw new IOException("Unable to read process output", e.getCause());
            }
            if (process.exitValue() != 0) {
                throw new ProcessExecutionException(errorMessage, process.exitValue(), output);
            }
            return output;
        } finally {
            // Terminate the process tree on timeout, interruption or output failure.
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            if (process.isAlive()) process.destroyForcibly();
            reader.cancel(true);
            process.getInputStream().close();
            process.getErrorStream().close();
        }
    }

    private String readOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    log.info("{}", line);
                    output.append(line).append("\n");
                }
            }
        }
        return output.toString().trim();
    }
}
