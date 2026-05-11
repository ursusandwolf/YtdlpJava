package com.ytdlpjava.core;

public class ProcessExecutionException extends ProcessException {
    private final int exitCode;
    private final String output;

    public ProcessExecutionException(String message, int exitCode, String output) {
        super("%s (Exit code: %d)".formatted(message, exitCode));
        this.exitCode = exitCode;
        this.output = output;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output;
    }
}
