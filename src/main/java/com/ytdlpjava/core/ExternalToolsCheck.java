package com.ytdlpjava.core;

/** Verifies external binaries required for a CLI task type. */
@FunctionalInterface
public interface ExternalToolsCheck {
    void verify(String taskType);
}
