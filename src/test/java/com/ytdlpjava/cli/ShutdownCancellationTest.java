package com.ytdlpjava.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ShutdownCancellationTest {
    @Test
    void jvmShutdownAllowsOwnerToCleanUpBeforeExit(@TempDir Path directory) throws Exception {
        Path cleanup = directory.resolve("cleanup.txt");
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"), Child.class.getName(), cleanup.toString())
                .redirectErrorStream(true).redirectOutput(directory.resolve("child.log").toFile()).start();
        try {
            assertTrue(process.waitFor(15, TimeUnit.SECONDS));
            assertEquals(130, process.exitValue());
            assertEquals("cleaned", Files.readString(cleanup));
        } finally {
            process.destroyForcibly();
        }
    }

    public static class Child {
        public static void main(String[] args) throws Exception {
            try (var cancellation = new ShutdownCancellation()) {
                Thread.ofPlatform().start(() -> System.exit(130));
                try {
                    Thread.sleep(60_000);
                } finally {
                    Files.writeString(Path.of(args[0]), "cleaned");
                }
            }
        }
    }
}
